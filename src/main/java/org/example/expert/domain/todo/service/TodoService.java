package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, String startDate, String endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todos;

        //endDate를 DateTime으로 변형했을때 해당 날짜의 00시 00분으로 변경되므로 하루 더하기
        if(endDate != null) {
            endDate = LocalDate.parse(endDate).plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        //아무것도 없는 경우
        if(weather == null && startDate == null && endDate == null) {todos = todoRepository.findAllByOrderByModifiedAtDesc(pageable);}

        //셋중 하나만 있는 경우
        else if(startDate == null && endDate == null) {todos = todoRepository.findAllByWeather(pageable, weather);}
        else if(weather == null &&  endDate == null) {todos = todoRepository.findAllByModifiedAtAfter(pageable, startDate);}
        else if(weather == null && startDate == null) {todos = todoRepository.findAllByModifiedAtBefore(pageable, endDate);}

        //셋중 두개만 있는 경우
        else if(endDate == null) {todos = todoRepository.findAllByWeatherAndModifiedAtAfter(pageable, weather, startDate);}
        else if(startDate == null) {todos = todoRepository.findAllByWeatherAndModifiedAtBefore(pageable, weather, endDate);}
        else if(weather == null) {todos = todoRepository.findAllByModifiedAtBetween(pageable, startDate, endDate);}

        //다있는 경우
        else {todos = todoRepository.findAllByWeatherAndModifiedAtBetween(pageable,weather,startDate,endDate);}


        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }
}
