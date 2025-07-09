package service;

import model.Operation;
import repository.OperationRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с операциями
 */
public class OperationService {
    /**
     * Репозиторий для работы с операциями
     */
    private final OperationRepository operationRepository;
    
    /**
     * Пользователь, выполняющий операции
     */
    private final String user;

    /**
     * Конструктор для сервиса
     * @param operationRepository репозиторий для работы с операциями
     * @param user пользователь, выполняющий операции
     */
    public OperationService(OperationRepository operationRepository, String user) {
        this.operationRepository = operationRepository;
        this.user = user;
    }

    /**
     * Конструктор для сервиса с автоматическим созданием репозитория
     * @param user пользователь, выполняющий операции
     */
    public OperationService(String user) {
        this.operationRepository = new OperationRepository("budget_master.db");
        this.user = user;
    }

    
} 