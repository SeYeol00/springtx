package hello.springtx.propagation;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config{

        @Bean // 빈을 직접 등록하면 내가 등록한 빈을 사용한다.
        public PlatformTransactionManager transactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit(){
        log.info("트랜잭션 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(transaction);
        log.info("트랜잭션 커밋 완료");

    }

    @Test
    void rollback(){
        log.info("트랜잭션 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.rollback(transaction);
        log.info("트랜잭션 커밋 완료");

    }

    @Test
    void double_commit(){

        log.info("트랜잭션1 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(transaction1);

        log.info("트랜잭션2 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManager.commit(transaction2);
    }

    @Test
    void double_commit_rollback(){

        log.info("트랜잭션1 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(transaction1);

        log.info("트랜잭션2 시작"); // 트랜잭션 시작하기
        TransactionStatus transaction2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 롤백 시작");
        txManager.rollback(transaction2);
    }

    @Test
    void inner_commit(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}",outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}",inner.isNewTransaction());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);
        // 내부에서 커밋해도 물리 트랜잭션은 끝나지 않아서
        // 실제로는 커밋이 일어나지 않는다.
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
        // 이 때 커밋이 실행된다.
        // 즉 내부 트랜잭션은 외부 트랜잭션이 커밋하기 전에는 커밋되지 않는다.
        // 외부 트랜잭션만 물리 트랜잭션을 커밋하고 내부는 커밋이 안된다.
        // 스프링은 외부 트랜잭션이 물리 트랜잭션을 관리하도록 한다.
        // 내부 트랜잭션은 기존 트랜잭션에 참여한다.
        // ==> 아무것도 안 한다는 것이다.
    }
    @Test
    void outer_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        // 외부 트랜잭션이 물리 트랜잭션 시작

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outer);
    }

    @Test
    void inner_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());


        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); // rollback-only 표시
        // 내부 트랜잭션을 롤백하면 실제 물리 트랜잭션은 롤백하지 않는다.
        // 대신에 기존 트랜잭션을 롤백 전용으로 표시한다.
        // 여기서 롤백 온리 표시가 나왔기 때문에
        // 롤백만 되어야한다.
        log.info("외부 트랜잭션 커밋");
        Assertions.assertThatThrownBy(()->txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);

    }

    @Test
    void inner_rollback_requires_new(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction={}",outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        // 여기서 분리를 시킨다.
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        // 내부와 외부를 분리시킨다. 핵심
        // setter를 통해서 프로퍼게이션비헤비어에 값을 넣어준다.
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus inner = txManager.getTransaction(definition);
        // 이러면 아예 분리되어서 이너를 만들 수 있다.
        log.info("inner.isNewTransaction()={}",inner.isNewTransaction());//true

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner);

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);

    }

}
