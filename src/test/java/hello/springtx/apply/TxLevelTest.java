package hello.springtx.apply;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@SpringBootTest
public class TxLevelTest {

    static class TxLevelTestConfig{
        @Bean
        LevelService levelService(){
            return new LevelService();
        }
    }


    @Slf4j
    @Transactional(readOnly = true)
    static class LevelService{

        @Transactional(readOnly =false)
        public void write(){
            log.info("call write");
            printTxInfo();
        }
        public void read(){
            log.info("call read");
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
