package dna.persistence.spring;

import dna.origins.commons.ResObject;
import dna.origins.commons.SerializableEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Collection;

//@Aspect
//@Component
public class DAOInterceptor {

    @Pointcut("@target(org.springframework.stereotype.Controller))")
    private void controllerMethod() {
    }

    @Pointcut("controllerMethod()&&execution(public org.springframework.http.ResponseEntity dna..*.*(..))")
    private void restfulAPI() {
    }//定义一个切入点

//    @Before("execution(* dna..*.service.*.*(..)))")
//    public void doAccessCheck(JoinPoint joinPoint) {
//        System.out.println("前置通知:"+joinPoint.getTarget().getClass().getSimpleName());
//    }
//
//    @AfterReturning("pointcut()")
//    public void doAfter() {
//        System.out.println("后置通知");
//    }
//
//    @After("pointcut()")
//    public void after() {
//        System.out.println("最终通知");
//    }
//
//    @AfterThrowing("pointcut()")
//    public void doAfterThrow() {
//        System.out.println("例外通知");
//    }

    @Around("restfulAPI()")
    public Object doBasicProfiling(ProceedingJoinPoint point) throws Throwable {
        //当前拦截的类和方法：
        Class clazz = point.getTarget().getClass();
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        System.out.println("进入环绕通知");
        Object object = point.proceed();//执行该方法
        Object resBody = ((ResObject) object).getBody();

        System.out.println("退出方法");
        return object;
    }

    private void writeDicValue(Object resBody) {
        String cid;
        if (resBody instanceof SerializableEntity) {
            ((SerializableEntity) resBody).getCid();
        }
        if (resBody instanceof Collection) {
            ((Collection) resBody).forEach(o -> {
                if (o instanceof SerializableEntity) {

                }
            });
        }
    }
}
