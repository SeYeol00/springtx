package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    /**
     * memberService    @Transactional: OFF
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON
     */

    @Test
    void outerTxOff_success(){
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);

        //when: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transactional: OFF
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON Exception
     */

    @Test
    void outerTxOff_fail(){
        //given
        String username = "로그예외_outerTxOff_fail";

        //when
        org.assertj.core.api.Assertions.assertThatThrownBy(()->memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        //when: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        // 로그는 단독적으로 롤백이 되서 empty가 되어야한다.
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: OFF
     * logRepository    @Transactional: OFF
     */

    @Test
    void singleTx(){
        //given
        String username = "outerTxOff_success";

        //when
        memberService.joinV1(username);
        // 서비스 단에서 어노테이션 트랜잭셔널을 걸면 하나의 커넥션으로
        // 이루어지는 거임

        //when: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON
     */

    @Test
    void outerTxOn_success(){
        //given
        String username = "outerTxOn_success";

        //when
        memberService.joinV1(username);
        // 서비스 단에서 어노테이션 트랜잭셔널을 걸면 하나의 커넥션으로
        // 이루어지는 거임

        //when: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }


    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON Exception
     */

    @Test
    void outerTxOn_fail(){
        // 서비스, 레포지토리 모두 트랜잭셔널이 걸려있는데 익셉션이 나면 전체 롤백
        // 롤백 온리로 설정이 된다.
        // 결국 최상단 외부 트랜잭션이 물리 트랜잭션이 되는데 그럼 커넥션 풀을 하나 쓴다.
        // 여기서 내부에서 익셉션이 뜨면 불난게 그냥 밖까지 넘어 오는 것이다.
        // 이러면 데이터 정합성에 문제가 생기지 않는다.
        // 결국 하나의 트랜잭션으로 묶이기 때문이다.

        //given
        String username = "로그예외_outerTxOn_fail";

        //when
        org.assertj.core.api.Assertions.assertThatThrownBy(()->memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        //when: 모든 데이터가 롤백되어야한다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON Exception
     */
    @Test
    void recoverException_fail(){
        // 최상단에서 익셉션을 잡은 로직
        // 하지만 그대로 다 롤백 되어버렸다.
        // 롤백 온리가 그대로 적용된 상태
        // 최상단에서 익셉션을 잡는다고해도 애초에
        // 익셉션이 터진 시점에서 롤백 온리로 설정이 바뀌어 버린다.
        // 즉 물리 트랜잭션이 이미 롤백 온리로 설정이 되어버려서
        // 최상단에서 잡는 로직을 추가해도 롤백이 되어버린다.

        //given
        String username = "로그예외_recoverException_fail";

        //when
        org.assertj.core.api.Assertions.assertThatThrownBy(()->memberService.joinV2(username))
                .isInstanceOf(RuntimeException.class);


        //when: 모든 데이터가 롤백되어야한다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transactional: ON
     * memberRepository @Transactional: ON
     * logRepository    @Transactional: ON(REQUIRES_NEW) Exception
     */
    @Test
    void recoverException_success(){
        // 최상단에서 익셉션을 잡은 로직
        // 하지만 그대로 다 롤백 되어버렸다.
        // 롤백 온리가 그대로 적용된 상태
        // 최상단에서 익셉션을 잡는다고해도 애초에
        // 익셉션이 터진 시점에서 롤백 온리로 설정이 바뀌어 버린다.
        // 즉 물리 트랜잭션이 이미 롤백 온리로 설정이 되어버려서
        // 최상단에서 잡는 로직을 추가해도 롤백이 되어버린다.

        //given
        String username = "로그예외_recoverException_success";

        //when
        memberService.joinV2(username);



        //when: member 저장, log가 롤백.
        assertTrue(memberRepository.find(username).isPresent());
        // 여기까지는 정상 커밋

        // logRepository에서는 REQURIES_NEW를 사용했기 때문에
        // 물리 트랜잭션을 하나 따로 생성해서 돌아간 것
        // 별도의 디비 커넥션으로 진행된다.
        // 여기 물리 트랜잭션은 롤백이 된다.
        assertTrue(logRepository.find(username).isEmpty());
    }
}