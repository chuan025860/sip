package org.chyunn_web.service;

import org.chyunn_web.bean.RandomSampling_All;
import org.chyunn_web.bean.RandomSampling_IT;
import org.chyunn_web.repository.RandomSampling_All_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RandomSampling_All_Service {
    @Autowired
    RandomSampling_All_Repository randomSamplingAllRepository;

    public RandomSampling_All saveRandomSampling(RandomSampling_All randomSamplingAll) {
        return randomSamplingAllRepository.save(randomSamplingAll);
    }
    public List<String> findIdsByStateFalse(){
        return randomSamplingAllRepository.findIdsByStateFalse();
    };
    public List<RandomSampling_All> findByRandomSampling() {
        return randomSamplingAllRepository.findByRandomSampling();
    }

    public List<String> findIds(){
        return randomSamplingAllRepository.findIds();
    };

    public RandomSampling_All getRandomSampling_All(String id) {
        Optional<RandomSampling_All> optional = randomSamplingAllRepository.findById(id);
        if (optional.isPresent()) {
            RandomSampling_All randomSamplingAll = optional.get();
            return randomSamplingAll;
        } else {
            return null;
        }
    }
}
