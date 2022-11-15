package hello.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;


@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired OrderService service;
    @Autowired OrderRepository repository;

    @Test
    void order() throws NotEnoughMoneyException {
        //given
        Order order = new Order();
        order.setUsername("정상");

        //when
        service.order(order);

        //then
        Order findOrder = repository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    @Test
    void runtimeException() {
        //given
        Order order = new Order();
        order.setUsername("예외");

        //when
        // 런타임 익셉션이 떴으니 롤백이 되어야한다.
        assertThatThrownBy(()->service.order(order))
                .isInstanceOf(RuntimeException.class);

        //then
        // 롤백이 되서 디비에 안 담겨있어야한다.
        Optional<Order> orderOpt = repository.findById(order.getId());
        assertThat(orderOpt.isEmpty()).isTrue();
    }

    @Test
    void bizException()  {
        //given
        Order order = new Order();
        order.setUsername("잔고부족");

        //when
        // 비즈니스 예외가 떴으니 대기가 되어야한다.
        // 이러면 일단 커밋이 되야한다.

        try {
            service.order(order);
        }catch(NotEnoughMoneyException e){
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입급하도록 안내");
        }

        //then
        // 롤백이 되서 디비에 안 담겨있어야한다.
        Order findOrder = repository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}