package com.asyncexport.boot.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.asyncexport.boot.base.BaseService;
import com.asyncexport.boot.entity.BzAsyncExportLog;
import com.asyncexport.boot.entity.PageQuery;
import com.asyncexport.boot.entity.TCmkDisposeExportDTO;
import com.asyncexport.boot.mapper.BzAsyncExportLogMapper;
import com.asyncexport.boot.mapper.TCmkDisposeMapper;
import com.asyncexport.boot.utils.RedisHelper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * 异步导出表
 *
 * @Author WangBj
 * @date 2023-01-09 06:17:47
 */
@Service
@Slf4j
public class BzAsyncExportLogService extends BaseService<BzAsyncExportLogMapper, BzAsyncExportLog> {

    @Resource
    ApplicationContext context;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    TCmkDisposeMapper tCmkDisposeMapper;

    @Value("${ae.saveType}")
    private String saveType = "";

    @Value("${ae.pageSize}")
    private Integer pageSize = 100;

    private static double spiltMax = 5000.00;
    private static int spiltMaxInt = 5000;



    public Page<TCmkDisposeExportDTO> getPage(PageQuery<BzAsyncExportLog> pageQuery) {
        List<TCmkDisposeExportDTO> list = new ArrayList<>();
        if (redisHelper.hasKey("bz_export_page")) {
            list = (List<TCmkDisposeExportDTO>) redisHelper.get("bz_export_page");
        } else {
            QueryWrapper<TCmkDisposeExportDTO> queryWrapper = new QueryWrapper<>();
//            queryWrapper.last("limit 200000");
            list = tCmkDisposeMapper.selectList(queryWrapper);
            redisHelper.set("bz_export_page", list, 60, TimeUnit.MINUTES);
        }

        Page<TCmkDisposeExportDTO> page = new Page<>();
        page.setRecords(list);
        return page;
    }

    /**
     * 新增
     *
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public BzAsyncExportLog insert(Object param, String name, String mathedPath) {
        BzAsyncExportLog bzAsyncExportLog = new BzAsyncExportLog();
        bzAsyncExportLog.setName(name);
        bzAsyncExportLog.setMethodPath(mathedPath);
        if (ObjectUtil.isNotNull(param))
            bzAsyncExportLog.setParams(JSONObject.toJSONString(param));

        this.baseMapper.insert(bzAsyncExportLog);
        bzAsyncExportLog.setId(bzAsyncExportLog.getId());
        return bzAsyncExportLog;
    }


    public void export() {
        //获取所有的导出任务
        LambdaQueryWrapper<BzAsyncExportLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BzAsyncExportLog::getState, 0).or().eq(BzAsyncExportLog::getState, 2);//只拿导入中的和失败的
        queryWrapper.eq(BzAsyncExportLog::getIsDelete, 0);
        queryWrapper.le(BzAsyncExportLog::getExportCount, 3);
        List<BzAsyncExportLog> list = baseMapper.selectList(queryWrapper);

        list.forEach(item -> {
            try {
                //加锁防止重复导出
                String buildKey = "----:bz:async:export:" + item.getId();

                RLock lock = redissonClient.getLock(buildKey);

                if (lock.tryLock()) {
                    try {
                        openMain(item, null);
                    } finally {
                        lock.unlock();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("导出失败:[{}]", JSONObject.toJSONString(item));
            }
        });

    }


    public void openMain(BzAsyncExportLog item, HttpServletResponse response) throws Exception {

        String headPath[] = item.getMethodPath().split("\\.");
        String serviceName = captureName(headPath[0]);
        //处理任务
        String servicePath = context.getBean(serviceName).getClass().getName().substring(0, context.getBean(serviceName).getClass().getName().indexOf("$"));
        //拿到service
        Class<?> clazz = Class.forName(servicePath);
        Object serviceBean = context.getBean(clazz);
        //获取当前service下所有的方法 准备遍历
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            //获取方法的名称
            String methodName = method.getName();

            // 返回对象类名
            String returnClassName = "";

            // 命中目标方法
            if (methodName.startsWith(headPath[1])) {

                File excelFile = null;

                try {

                    // 获取方法返回对象类型
                    returnClassName = getReturnClassName(method, methodName, returnClassName);

                    // 分页查询
                    Object returnParam = invokePage(item, serviceBean, method, 1);

                    // 创建容器
                    List<Object> allList = new ArrayList<>();

                    // 先放入第一页
                    Object returnList = ((Page<?>) returnParam).getRecords();
                    allList.addAll((Collection<?>) returnList);

                    // 总页数
                    long pages = ((Page<?>) returnParam).getPages();

                    // 遍历查询
                    if (pages > 1) {
                        for (int page = 2; page <= pages; ++page) {
                            Object nextPageReturn = invokePage(item, serviceBean, method, page);
                            Object nextReturnList = ((Page<?>) nextPageReturn).getRecords();
                            allList.addAll((Collection<?>) nextReturnList);
                        }
                    }

                    //构建excel 上传至oss
                    log.info("BzAsyncExportLogService openMain 开始生成EXCEL");
                    //生成EXCEL 上传至OSS

                    //同步处理直接返回二进制文件
                    if (item.getSyncFlag() == 1 && ObjectUtil.isNotNull(response)) {
                        log.info("BzAsyncExportLogService openMain 同步处理生成生成二进制文件 ");
                        String fileName = URLEncoder.encode(item.getName(), "UTF-8");
                        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                        response.setCharacterEncoding("utf-8");
                        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
                        splitListAndExport(response, returnClassName, allList);
                        return;
                    }

                    excelFile = File.createTempFile(item.getName() + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm"), ".xlsx");
                    log.info("BzAsyncExportLogService openMain 开始异步处理文件生成");
                    splitListAndExport(excelFile, returnClassName, allList);
                    log.info("BzAsyncExportLogService openMain 生成二进制文件 ");
                    byte[] bytes = FileUtil.readBytes(excelFile);
                    //  输出路径

                    String fileCode = "";

                    if (StrUtil.isNotBlank(item.getOutMethodPath()) && "custom".equals(saveType)) {

                        fileCode = getFileCode(item, clazz, (Object) bytes, fileCode);

                    } else if ("local".equals(saveType)) {
                        //将文件输出至本地
                        localExport(item, bytes);
                        fileCode = "src/main/resources/";
                    }
                    item.setOperationCode(fileCode).setState(1);
                } catch (IOException e) {
                    item.setState(0);
                    e.printStackTrace();
                } finally {
                    if (excelFile != null) {
                        excelFile.delete();
                    }

                    //异步情况落入表数据
                    if (item.getSyncFlag() == 0) {
                        item.setExportCount(item.getExportCount() + 1);
                        this.baseMapper.updateById(item);
                    }
                }
            }
        }


    }

    private Object invokePage(BzAsyncExportLog item, Object serviceBean, Method method, long page) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        //开始构建参数 拿到返回数据 执行导出
        //目前参数是对象方式传输 拿第一个对象即可
        Class parameter = method.getParameterTypes()[0];
        //如果是同步返回情况 那么第二个参数一定要是Response;
        Object parameterInstance = parameter.newInstance();//参数对象
        JSONObject jsonObject = new JSONObject();//参数
        if (parameterInstance instanceof PageQuery) {

            toJSONObject(item, method, page, jsonObject);

        } else {
            //数据传输时一定是 PageQuery类型 但是方法入参类型不一定是
            //需要取出来其中的param
            jsonObject = StrUtil.isBlank(item.getParams()) ? null : JSONObject.parseObject(item.getParams());
            jsonObject = jsonObject == null ? null : JSONObject.parseObject(JSONObject.toJSONString(jsonObject.get("param")));
        }


        //给所有的字段设置值
        if (ObjectUtil.isNotNull(jsonObject)) {
            setField(parameter, parameterInstance, jsonObject);
        }

        Object returnParam = null;

        //参数全部设置完毕 拿到返回数据
        returnParam = method.invoke(serviceBean, parameterInstance);
        return returnParam;
    }

    private void toJSONObject(BzAsyncExportLog item, Method method, long page, JSONObject jsonObject) throws InstantiationException, IllegalAccessException {
        //  将当前参数转化为当前方法的入参类型
        Type[] parameterTypes = method.getGenericParameterTypes();
        for (Type paramType : parameterTypes) {
            //ParameterizedType:参数化类型，判断是否是参数化类型。
            if (paramType instanceof ParameterizedType) {
                //获得源码中的真正的参数类型
                Type[] genericType = ((ParameterizedType) paramType).getActualTypeArguments();
                Class<?> actualTypeArgument = (Class<?>) genericType[0];
                JSONObject params = (StrUtil.isBlank(item.getParams()) ? null : JSONObject.parseObject(item.getParams()));
                Object requestParam = actualTypeArgument.newInstance();
                //放上对象
                setField(actualTypeArgument, requestParam, params);

                jsonObject.put("param", requestParam);
                // 第一页
                jsonObject.put("page", page);
                jsonObject.put("size", pageSize);
                break;
            }

        }
    }

    private String getReturnClassName(Method method, String methodName, String returnClassName) {
        //返回一个Type对象，表示由该方法对象表示的方法的正式返回类型。
        //比如public List<User> getAll();那么返回的是List<User>
        Type genericReturnType = method.getGenericReturnType();
        //获取实际返回的参数名
        String returnTypeName = genericReturnType.getTypeName();
        System.out.println(methodName + "的返回参数是：" + returnTypeName);
        //判断是否是参数化类型
        if (genericReturnType instanceof ParameterizedType) {
            //如果是参数化类型,则强转
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            //获取实际参数类型数组，比如List<User>，则获取的是数组[User]，Map<User,String> 则获取的是数组[User,String]
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type type : actualTypeArguments) {
                //强转
                Class<?> actualTypeArgument = (Class<?>) type;
                //获取实际参数的类名
                returnClassName = actualTypeArgument.getName();
                System.out.println(methodName + "的返回值类型是参数化类型，其类型为：" + returnClassName);
            }

        } else {
            //不是参数化类型,直接获取返回值类型
            Class<?> returnType = method.getReturnType();
            //获取返回值类型的类名
            returnClassName = returnType.getName();
            System.out.println(methodName + "的返回值类型不是参数化类型其类型为：" + returnClassName);
        }
        return returnClassName;
    }

    /**
     * 转化为JSONObject
     *
     * @param item
     * @param method
     * @param jsonObject
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private void toJSONObject(BzAsyncExportLog item, Method method, JSONObject jsonObject) throws InstantiationException, IllegalAccessException {

    }

   /**
     * 指定输出方法
     *
     * @param item
     * @param clazz
     * @param bytes
     * @param fileCode
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
    */
    private String getFileCode(BzAsyncExportLog item, Class<?> clazz, Object bytes, String fileCode) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
        //获取导出文件路径
        String outHeadPath[] = item.getOutMethodPath().split("\\.");
        //获取导出文件名称
        String outServiceName = captureName(outHeadPath[0]);
        //处理任务
        String outServicePath = context.getBean(outServiceName).getClass().getName().substring(0, context.getBean(outServiceName).getClass().getName().indexOf("$"));
        //拿到service
        Class<?> outClazz = Class.forName(outServicePath);
        Object outServiceBean = context.getBean(clazz);
        //获取当前service下所有的方法 准备遍历
        Method[] outMethods = outClazz.getMethods();
        for (Method outMethod : outMethods) {
            if (outMethod.getName().startsWith(outHeadPath[1])) {
                fileCode = outMethod.invoke(outServiceBean, bytes).toString();
                break;
            }
        }
        return fileCode;
    }

    /**
     * 本地执行
     *
     * @param item
     * @param bytes
     * @throws IOException
     */
   private void localExport(BzAsyncExportLog item, byte[] bytes) throws IOException {
        //获取文件名
        String fileName = item.getName() + DateUtil.format(new Date(), "yyyy-MM-dd HH:mm") + ".xlsx";
        //输出到项目路径下
        FileOutputStream outStream = null;
        try {
            File file = new File("src/main/resources/" + fileName);//文件路径（路径+文件名）
            if (!file.exists()) {   //文件不存在则创建文件，先创建目录
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            outStream = new FileOutputStream(file); //文件输出流将数据写入文件
            outStream.write(bytes);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                outStream.close();
            }
        }
    }

    /**
     * 执行导出文件生成 并切割集合
     *
     * @param obj
     * @param name
     * @param returnParam
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void splitListAndExport(Object obj, String name, List returnParam) throws IOException, ClassNotFoundException {
       //判断返回参数是否大于5000条
        if (returnParam.size() > spiltMaxInt) {

            log.info("--------数据大于5000 开始分割sheet ------【{}】", returnParam.size());

            List<List> splitList = new ArrayList<>();
            //每个sheet 最多不能超过5000个 所以需要分割集合
            for (int i = 0; i < returnParam.size(); i += spiltMaxInt) {
                splitList.add(returnParam.subList(i, Math.min(i + spiltMaxInt, returnParam.size())));
            }
            log.info("---------数据分割共计 sheet页 【{}个】", splitList.size());
            //创建excel
            ExcelWriter excelWriter = null;
            //判断类型
            if (obj instanceof HttpServletResponse) {
                log.info("====识别为同步处理=====");
                HttpServletResponse httpServletResponse = (HttpServletResponse) obj;
                excelWriter = EasyExcel.write(httpServletResponse.getOutputStream(), Class.forName(name)).build();
            }

            if (obj instanceof File) {
                log.info("======识别为异步处理========");
                File file = (File) obj;
                excelWriter = EasyExcel.write(file, Class.forName(name)).build();
            }

            // 遍历分割后的list
            for (int i = 0; i < splitList.size(); i++) {
                // 创建sheet
                WriteSheet writeSheet = EasyExcel.writerSheet(i, "sheet_" + (i + 1)).build();

                // 写入数据
                excelWriter.write(splitList.get(i), writeSheet);

                log.info("分割第【{}个】 完成！", i + 1);
            }
            log.info("======================分割完成 开始输出文件======================");
            excelWriter.finish();
        } else {
            //如果小于5000条，则直接写入
            if (obj instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) obj;
                EasyExcel.write(httpServletResponse.getOutputStream(), Class.forName(name)).sheet("sheet").doWrite(returnParam);
            }
            if (obj instanceof File) {
                File file = (File) obj;
                EasyExcel.write(file, Class.forName(name)).sheet("sheet").doWrite(returnParam);
            }

        }
    }

    public static void main(String[] args) {
//        List<Integer> returnParam = new ArrayList<>();
//        for (int i = 0; i < 10001; i++) {
//            returnParam.add(i);
//        }
//        int size = (int) Math.ceil(returnParam.size() / spiltMax);
//        Collection<? extends List<?>> values = returnParam.stream().collect(Collectors.groupingBy(i -> (int) (i.hashCode() % size))).values();
//        System.out.println(values.size());


    }

    //首字母小写
   public static String captureName(String name) {
        char[] cs = name.toCharArray();
        //已经是小写
        if (cs[0] >= 97) {
            return name;
        }
        cs[0] += 32;
        return String.valueOf(cs);

    }


    /**
     * 反射字段赋值
     *
     * @param parameter  目标对象class
     * @param o          需要被赋值的目标对象
     * @param jsonObject 参数
     * @throws IllegalAccessException
     */
   public void setField(Class parameter, Object o, JSONObject jsonObject) throws IllegalAccessException {
        Field[] fields = parameter.getDeclaredFields();//所有字段
        Field.setAccessible(fields, true);//可以拿到私有变量
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            //判断是否为final类型
            if (java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            String paraName = field.getName();
            //判断是否为空
            if (ObjectUtil.isNull(jsonObject.get(paraName)) || StrUtil.isBlank(jsonObject.get(paraName).toString())) {
                continue;
            }
            //处理日期
            if (field.getType() == Date.class && jsonObject.get(paraName).getClass() == Long.class) {
                Date date = new Date();
                date.setTime((long) jsonObject.get(paraName));
                field.set(o, date);
            } else if (field.getType() == List.class) {
                List list = (List) jsonObject.get(paraName);
                if (CollectionUtil.isNotEmpty(list) && list.get(0).getClass() == Long.class) {
                    List<Date> dateList = new ArrayList<>();
                    for (int i1 = 0; i1 < list.size(); i1++) {
                        Date date = new Date();
                        date.setTime((long) list.get(i1));
                        dateList.add(date);
                    }
                    field.set(o, dateList);
                } else {
                    field.set(o, list);
                }
            } else {
                field.set(o, jsonObject.get(paraName));
            }
        }
    }

}

