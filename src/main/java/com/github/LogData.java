package com.github;


import java.util.Date;

/**
 * @author EalenXie Created on 2019/12/23 16:46.
 * Log data object (no external constructor is provided; there is only one such object per thread)
 * 自定义日志数据对象 (不提供对外的构造方法,每个线程中仅有一个此对象)
 */
public class LogData {

    private LogData() {
    }

    /**
     * 请务必注意该对象 使用->释放 原则
     */
    private static final ThreadLocal<LogData> LOG_DATA = new ThreadLocal<>();
    /**
     * 应用名
     */
    private String appName;
    /**
     * 主机
     */
    private String host;
    /**
     * 端口号
     */
    private Integer port;
    /**
     * 请求Ip
     */
    private String clientIp;
    /**
     * 请求地址
     */
    private String reqUrl;
    /**
     * http请求method
     */
    private String httpMethod;
    /**
     * 请求头部信息(可选择记录)
     */
    private Object headers;
    /**
     * 操作类型
     */
    private String type;
    /**
     * 方法内容
     */
    private String content;
    /**
     * 操作方法
     */
    private String method;
    /**
     * 参数
     */
    private Object args;
    /**
     * 响应体
     */
    private Object respBody;
    /**
     * 操作日期(调用日期)
     */
    private Date logDate;
    /**
     * 业务处理耗时
     */
    private long costTime;
    /**
     * 线程名
     */
    private String threadName = Thread.currentThread().getName();
    /**
     * 线程Id
     */
    private long threadId = Thread.currentThread().getId();
    /**
     * 执行状态 成功(true)/异常(false)  默认失败false
     */
    private boolean success = false;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getReqUrl() {
        return reqUrl;
    }

    public void setReqUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Object getHeaders() {
        return headers;
    }

    public void setHeaders(Object headers) {
        this.headers = headers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getArgs() {
        return args;
    }

    public void setArgs(Object args) {
        this.args = args;
    }

    public Object getRespBody() {
        return respBody;
    }

    public void setRespBody(Object respBody) {
        this.respBody = respBody;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public long getCostTime() {
        return costTime;
    }

    public void setCostTime(long costTime) {
        this.costTime = costTime;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * 耗时计算
     */
    public void toCostTime() {
        LogData data = LogData.getCurrent();
        data.setCostTime((System.currentTimeMillis() - logDate.getTime()));
        LogData.setCurrent(data);
    }

    /**
     * 获取当前线程中的操作日志对象
     */
    public static LogData getCurrent() {
        LogData data = LOG_DATA.get();
        if (data == null) {
            data = new LogData();
            data.setLogDate(new Date());
            LOG_DATA.set(data);
        }
        return LOG_DATA.get();
    }

    public static void setCurrent(LogData data) {
        LOG_DATA.set(data);
    }

    /**
     * 移除当前线程操作日志对象
     */
    public static void removeCurrent() {
        LOG_DATA.remove();
    }

    /**
     * 内容记录 正常会在aop中结束释放
     *
     * @param step 这里可以使用 该方法记录每一个步骤
     */
    public static void step(String step) {
        LogData data = LOG_DATA.get();
        if (data != null) {
            if (data.getContent() == null) data.setContent("");
            data.setContent(data.getContent() + step + "\n");
            setCurrent(data);
        }
    }

    /**
     * 内容记录，支持模板及参数列表
     *
     * @param describeTemplate
     * @param args
     */
    public static void step(String describeTemplate, Object... args) {
        step(format(describeTemplate, args));
    }

    /**
     * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
     * position: the first {@code %s} gets {@code args[0]}, etc. If there are more arguments than
     * placeholders, the unmatched arguments will be appended to the end of the formatted message in
     * square braces.
     *
     * @param template a non-null string containing 0 or more {@code %s} placeholders.
     * @param args     the arguments to be substituted into the message template. Arguments are converted
     *                 to strings using {@link String#valueOf(Object)}. Arguments can be null.
     */
    // Note that this is somewhat-improperly used from Verify.java as well.
    static String format(String template, Object... args) {
        template = String.valueOf(template); // null -> "null"

        // start substituting the arguments into the '%s' placeholders
        StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
        int templateStart = 0;
        int i = 0;
        while (i < args.length) {
            int placeholderStart = template.indexOf("%s", templateStart);
            if (placeholderStart == -1) {
                break;
            }
            builder.append(template, templateStart, placeholderStart);
            builder.append(args[i++]);
            templateStart = placeholderStart + 2;
        }
        builder.append(template, templateStart, template.length());

        // if we run out of placeholders, append the extra args in square braces
        if (i < args.length) {
            builder.append(" [");
            builder.append(args[i++]);
            while (i < args.length) {
                builder.append(", ");
                builder.append(args[i++]);
            }
            builder.append(']');
        }

        return builder.toString();
    }
}
