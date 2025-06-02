package com.unihub.app.config;

import com.unihub.app.entity.Curso;
import com.unihub.app.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CursoRepository cursoRepository;

    @Autowired
    public DataInitializer(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Override
    @Transactional // Add transactional to ensure operations are within a transaction
    public void run(String... args) throws Exception {
        List<String> cursoNomes = Arrays.asList(
                "Engenharia da Computacao",
                "Ciencia da Computacao",
                "Sistemas de Informacao"
        );

        for (String nome : cursoNomes) {
            if (cursoRepository.findByNome(nome).isEmpty()) {
                Curso curso = new Curso(nome);
                cursoRepository.save(curso);
                System.out.println("Created curso: " + nome);
            }
        }
    }
} 