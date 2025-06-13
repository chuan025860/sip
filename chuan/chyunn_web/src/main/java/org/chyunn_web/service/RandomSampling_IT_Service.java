package org.chyunn_web.service;

import org.chyunn_web.bean.Asset.RandomSampling_IT;
import org.chyunn_web.repository.RandomSampling_IT_Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RandomSampling_IT_Service {
    @Autowired
    RandomSampling_IT_Repository randomSamplingItRepository;

    public RandomSampling_IT saveRandomSampling(RandomSampling_IT randomSamplingIt) {
        return randomSamplingItRepository.save(randomSamplingIt);
    }
    public List<String> findIdsByStateFalse(){
        return randomSamplingItRepository.findIdsByStateFalse();
    };

    public List<String> findIds(){
        return randomSamplingItRepository.findIds();
    };

    public List<RandomSampling_IT> findByRandomSampling() {
        return randomSamplingItRepository.findByRandomSampling();
    }

    public RandomSampling_IT getRandomSampling_IT(String id) {
        Optional<RandomSampling_IT> optional = randomSamplingItRepository.findById(id);
        if (optional.isPresent()) {
            RandomSampling_IT randomSamplingIt = optional.get();
            return randomSamplingIt;
        } else {
            return null;
        }
    }

}
