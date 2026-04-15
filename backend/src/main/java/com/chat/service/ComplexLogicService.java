package com.chat.service;

import org.springframework.stereotype.Service;
import java.util.Random;

@Service
public class ComplexLogicService {

    /**
     * [테스트 시나리오 2] 테스트 코드가 없는 복잡한 로직 추가
     * 이 코드는 New Code Coverage를 급격히 낮추어 퀄리티 게이트 실패를 유도합니다.
     */
    public String performComplexCalculation(int input) {
        if (input > 100) {
            return "Large";
        } else if (input > 50) {
            return "Medium";
        } else if (input > 0) {
            return "Small";
        } else {
            return "Negative or Zero";
        }
    }

    public void complicatedMethod() {
        Random random = new Random();
        int val = random.nextInt(10);
        
        switch (val) {
            case 1: System.out.println("One"); break;
            case 2: System.out.println("Two"); break;
            case 3: System.out.println("Three"); break;
            case 4: System.out.println("Four"); break;
            case 5: System.out.println("Five"); break;
            default: System.out.println("Other"); break;
        }
    }
}
