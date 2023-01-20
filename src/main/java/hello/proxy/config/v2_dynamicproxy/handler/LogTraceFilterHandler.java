package hello.proxy.config.v2_dynamicproxy.handler;

import hello.proxy.trace.TraceStatus;
import hello.proxy.trace.logtrace.LogTrace;
import org.springframework.util.PatternMatchUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class LogTraceFilterHandler implements InvocationHandler {

    private final Object target;
    private final LogTrace logTrace;
    private final String[] patterns;

    public LogTraceFilterHandler(Object target, LogTrace logTrace, String[] patterns) {
        this.target = target;
        this.logTrace = logTrace;
        this.patterns = patterns;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 메서드 이름 필터
        String methodName = method.getName();
        // save, request, reque*, *est
        if (!PatternMatchUtils.simpleMatch(patterns, methodName)) {
            return method.invoke(target, args); // 실제만 호출함
        }

        TraceStatus status = null;
        try {
            String message = method.getDeclaringClass().getSimpleName() + "." + method.getName() + "()";
            status = logTrace.begin(message);
            // 로직 호출

            Object result = method.invoke(target, args);
//            BInterface proxy = (BInterface) Proxy.newProxyInstance(
//                    BInterface.class.getClassLoader(), // 어느 클래스 로더에 할지
//                    new Class[]{BInterface.class}, // 어느 인터페이스를 기반으로 할지
//                    handler); // 어느 로직을 사용할지

//            String result = target.request(itemId);
            logTrace.end(status);
            return result;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
