package hello.springtx.propagation;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
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
    }

}
