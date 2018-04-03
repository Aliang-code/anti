package dna.persistence.springboot;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.*;
import org.springframework.transaction.jta.JtaTransactionManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class TxAdviceInterceptor {

    private static final String AOP_POINTCUT_EXPRESSION = "execution (* dna..*.service.*.*(..))";

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public TransactionInterceptor txAdvice() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
         /*只读事务，不做更新操作*/
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        /*当前存在事务就使用当前事务，当前不存在事务就创建一个新的事务*/
        RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
        requiredTx.setRollbackRules(
                Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        Map<String, TransactionAttribute> txMap = new HashMap<>();
        txMap.put("get*", readOnlyTx);
        txMap.put("query*", readOnlyTx);
        txMap.put("find*", readOnlyTx);
        txMap.put("load*", readOnlyTx);

        txMap.put("add*", requiredTx);
        txMap.put("save*", requiredTx);
        txMap.put("create*", requiredTx);
        txMap.put("insert*", requiredTx);
        txMap.put("modify*", requiredTx);
        txMap.put("update*", requiredTx);
        txMap.put("apply*", requiredTx);
        txMap.put("handle*", requiredTx);
        txMap.put("remove*", requiredTx);
        txMap.put("delete*", requiredTx);
        txMap.put("do*", requiredTx);
        txMap.put("upload*", requiredTx);
        txMap.put("receive*", requiredTx);
        txMap.put("send*", requiredTx);
        source.setNameMap(txMap);
        if (transactionManager instanceof JtaTransactionManager) {
            ((JtaTransactionManager) transactionManager).setAllowCustomIsolationLevels(true);
        }
        TransactionInterceptor txAdvice = new TransactionInterceptor(transactionManager, source);
        return txAdvice;
    }

    @Bean
    public Advisor txAdviceAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
        return new DefaultPointcutAdvisor(pointcut, txAdvice());
    }
}
