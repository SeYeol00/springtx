package hello.springtx.propagation;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {

    private EntityManager em;

    @Transactional
    public void save(Log logMessage){
        log.info("로그 저장");
        em.persist(logMessage);

        if(logMessage.getMessage().contains("로그 예외")){
            log.info("log 자장시 예외 발생");
            throw new RuntimeException("예외 발생");
            // 이 떄는 롤백된다.
        }
    }

    public Optional<Member> find(String username){
        return em.createQuery("select m from Member m where m.username = :username",Member.class)
                .setParameter("username",username)
                .getResultList().stream().findAny();
        // JPQL


    }
}
