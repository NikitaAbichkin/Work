package com.example.auth.service;

import com.example.auth.dto.AIPlanResponce;
import com.example.auth.dto.AiPlanRequest;
import com.example.auth.model.Goal;
import com.example.auth.model.User;
import com.example.auth.repository.GoalRepository;
import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import jakarta.servlet.http.HttpServlet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.mapper.Mapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiPlanService {
    @Value("${Ai.AI_API_KEY}")
    String AI_API_KEY;

    @Value("${Ai.AI_MODEL}")
    String AI_MODEL;

    @Value("${Ai.AI_TIMEOUT_MS}")
    Integer timout;

    @Value("${Ai.AiUrl}")
    String Aiurl;

    private final RestClient restClient;
    private GoalRepository goalRepository;
    private  final ObjectMapper objectMapper;
    private  final JwtService jwtService;
    private  final  UserRepository userRepository;

    public AiPlanService(ObjectMapper objectMapper, RestClient restClient,GoalRepository goalRepository,JwtService jwtService, UserRepository userRepository){
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.goalRepository = goalRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;

    }



    @SneakyThrows
    public  AIPlanResponce generatePlan(String token,String promt){
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("нет такого пользователя"));

        String systemPrompt = """
          Ты планировщик целей. Пользователь описывает что хочет достичь.
          Сгенерируй цель и список задач.                                                                                                          
          Верни ТОЛЬКО JSON, без пояснений, в формате:
          {                                                                                                                                        
            "title": "...",
            "description": "...",
            "priority": "LOW | MEDIUM | HIGH",
            "start_date": "YYYY-MM-DD",
            "deadline": "YYYY-MM-DD",
            "daily_time_minutes": 60,
            "stages": [
              {
                "title": "...",
                "description": "...",
                "priority": "LOW | MEDIUM | HIGH",
                "estimatedMinutes": 60,
                "deadline": "YYYY-MM-DD",
                "startsAt": "YYYY-MM-DD",
                "sortOrder": 1,
                "status": "IN_PROGRESS"
              }
            ]
          }
          """;


        Map<String, Object> body = Map.of(
                "model", AI_MODEL,
                "temperature", 0.7,
                "messages",List.of(
                        Map.of( "role", "system", "content", systemPrompt),
                               Map.of("role", "user", "content", promt)

                ));

        String responceBody =restClient.post()// 1. Метод запроса (POST)
                .uri(Aiurl)// 2. Куда отправляем
                .header("Authorization","Bearer "+AI_API_KEY)// 3. Заголовки
                .contentType(MediaType.APPLICATION_JSON)// 4. Тип тела = JSON
                .body(body)// 5. Само тело запроса
                .retrieve()// 6. Выполнить запрос
                .body(String.class);// 7. Получить ответ как String


        JsonNode root = objectMapper.readTree(responceBody);
        String content = root.at("/choices/0/message/content").asText();
        AIPlanResponce aiPlanResponce = objectMapper.readValue(content,AIPlanResponce.class);


        return  aiPlanResponce;

    }

    @SneakyThrows
    public  AIPlanResponce  HelpWithGoal(String token , String  promt, Long goalId) {
        Long userId = jwtService.extractId(token);
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("нет такого пользователя"));

        Goal goal = goalRepository.findByUserAndId(user,goalId).orElseThrow(()-> new IllegalArgumentException("нет такой цели "));

        String GoalString = objectMapper.writeValueAsString(goal);


        String systemPrompt = String.format("""
                Ты планировщик целей. Пользователь описывает что хочет достичь.
                Сгенерируй цель и список задач.                                                                                                          
                Верни ТОЛЬКО JSON, без пояснений, в формате:
                {                                                                                                                                        
                  "title": "...",
                  "description": "...",
                  "priority": "LOW | MEDIUM | HIGH",
                  "start_date": "YYYY-MM-DD",
                  "deadline": "YYYY-MM-DD",
                  "daily_time_minutes": 60,
                  "stages": [
                    {
                      "title": "...",
                      "description": "...",
                      "priority": "LOW | MEDIUM | HIGH",
                      "estimatedMinutes": 60,
                      "deadline": "YYYY-MM-DD",
                      "startsAt": "YYYY-MM-DD",
                      "sortOrder": 1,
                      "status": "IN_PROGRESS"
                    }
                  ]
                }
                текущая цель : %s
                """,GoalString);
        Map<String, Object> body = Map.of(
                "model", AI_MODEL,
                "temperature", 0.7,
                "messages",List.of(
                        Map.of( "role", "system", "content", systemPrompt ),
                        Map.of("role", "user", "content", promt)
                ));
        String responceBody =restClient.post()// 1. Метод запроса (POST)
                .uri(Aiurl)// 2. Куда отправляем
                .header("Authorization","Bearer "+AI_API_KEY)// 3. Заголовки
                .contentType(MediaType.APPLICATION_JSON)// 4. Тип тела = JSON
                .body(body)// 5. Само тело запроса
                .retrieve()// 6. Выполнить запрос
                .body(String.class);// 7. Получить ответ как String

        JsonNode root = objectMapper.readTree(responceBody);
        String content = root.at("/choices/0/message/content").asText();
        AIPlanResponce aiPlanResponce = objectMapper.readValue(content,AIPlanResponce.class);

        return aiPlanResponce;

    }

}
