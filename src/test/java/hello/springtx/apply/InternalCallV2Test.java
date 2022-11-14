package hello.springtx.apply;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;



// public 매서드에서만 트랜잭션이 적용된다. 이건 스프링 규칙
@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired CallService callService;

    @Test
    void printProxy(){
        log.info("callService class={}", callService.getClass());

    }

    @Test
    void externalCallV2(){
        callService.external();
    }


    @TestConfiguration
    static class InternalCallV1TestConfig{

        @Bean
        public CallService callService(){
            return new CallService(internalService());
        }// 생성자 호출

        @Bean
        public InternalService internalService(){
            return new InternalService();
        }

    }


    @Slf4j
    @RequiredArgsConstructor
    static class CallService{

        private final InternalService internalService;

        public void external(){
            log.info("call external");
            printTxInfo();
            internalService.internal();// 외부에서 요청이 와서 인터널 요청을 할 때
            // 트랜잭셔널이 걸린 함수를 외부에서 호출하면
            // 프록시 객체가 생성이 안되서 트랜잭션 반영이 안 된다!!!

            // 이렇게 트랜잭션이 없는 외부에서 요청이 오면 프록시 객체가 생성이 안 된다...
            // external()[트랜잭션 적용 X] -> internal()[트랜잭션 적용 O]
            // -> 프록시를 안 거치고 내가 내거 그냥 호출한 거다.
            // 즉 그냥 서비스가 프록시를 부른게 아니라 자기 자신의 매서드를 호출한거다.
            // 나 자신의 인스턴스의 함수를 호출한 것
            // 프록시 방식의 aop의 한계
            // 매서드 내부 호출에서는 aop나 어노테이션 적용이 안 된다.
        }



        private void printTxInfo(){
            boolean txActive = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("tx active = {}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly = {}",readOnly);
        }
    }

    // 해결방안 1, 별도의 클래스로 분리하기
    // 이러면 내부 호출이 아니라 외부 호출이 되기 때문에 해결된다.
    // 여기서 핵심은 클래스 단위로 분리 시켜서 혼선을 일어나지 않도록 하는 것이다.
    static class InternalService{

        @Transactional // CGLIB가 붙은 프록시 객체 생성, 이걸 바로 실행하면 프록시 객체가 생성됨
        public void internal(){
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo(){
            boolean txActive = TransactionSynchronizationManager.isSynchronizationActive();
            log.info("tx active = {}", txActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("tx readOnly = {}",readOnly);
        }
    }


}
