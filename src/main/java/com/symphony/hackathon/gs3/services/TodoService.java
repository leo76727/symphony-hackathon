package com.symphony.hackathon.gs3.services;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.symphony.hackathon.gs3.bot.TodoBot;
import com.symphony.hackathon.gs3.model.Status;
import com.symphony.hackathon.gs3.model.Todo;
import com.symphony.hackathon.gs3.symphony.utils.EntityDataHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.exceptions.StreamsException;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.clients.model.SymStreamTypes;
import org.symphonyoss.symphony.clients.model.SymUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    private final AtomicLong idGenerator = new AtomicLong(1);

    private SymphonyClient symClient;
    private ActorService actorService;
    private Map<Long, Todo> todos;
    private TodoRepo repo = new TodoRepo();
    Pattern taskIdPattern = Pattern.compile("\\s*\\#\\S+\\s(?:(?:TODO|TASK)-)?(\\d+).*", Pattern.CASE_INSENSITIVE);
    Pattern priorityPattern = Pattern.compile(".*p:([123]).*", Pattern.CASE_INSENSITIVE);

    public TodoService(SymphonyClient symClient, ActorService actorService){
        this.symClient = symClient;
        this.actorService = actorService;
        todos = repo.loadAll();
        idGenerator.set(todos.keySet().stream().mapToLong(l->l).max().orElse(0) + 1 );
    }
    public Todo createTask(SymMessage message) throws StreamsException {
        return createTasks(message, false).get(0);
    }
    public List<Todo> createTasks(SymMessage message) throws StreamsException {
        return createTasks(message, true);
    }

    private List<Todo> createTasks(SymMessage message, boolean recurring) throws StreamsException {
        String messageText = message.getMessageText()
                .replace("#task ", "")
                .replace("#taskr ", "");
        List<Todo> result = new ArrayList<>();

        Optional<SymUser> assignee = getAssignee(message);
        String assigneeName = assignee.map(SymUser::getDisplayName).orElse("Unassigned");
        Long assigneeId = assignee.map(SymUser::getId).orElse(null);
        List<String> labels = getLabels(messageText);
        int priority = getPriority(message.getMessageText(),2);
        System.out.println(labels);
        for(String label : labels){
            messageText = messageText.replace("#" + label,"");
        }


        DueDateMatch dueDateMatch;
        if(recurring){
            dueDateMatch = getDueDates(messageText);
        } else {
            dueDateMatch = getDueDate(messageText);
        }

        String summary = messageText
                .replace("@" + assigneeName, "")
                .replace("p:" + priority, "");
        if(dueDateMatch != null){
            summary = summary.replaceFirst("(at|in|by|every)?\\s*" + dueDateMatch.matchedText + ".*[^#@]", "");
        }


        for(LocalDateTime due : dueDateMatch.due){
            Todo todo = new Todo(
                    idGenerator.getAndIncrement(),
                    summary,
                    message.getSymUser().getDisplayName(),
                    message.getSymUser().getId(),
                    assigneeName,
                    assigneeId,
                    message.getStreamId(),
                    message.getStream().getStreamType() == SymStreamTypes.Type.ROOM ? symClient.getStreamsClient().getRoomDetail(message.getStreamId()).getRoomAttributes().getName() : "Personal",
                    due,
                    priority,
                    labels
            );
            save(todo);
            result.add(todo);
        }

        return result;
    }

    private int getPriority(String messageText, int defaultValue) {
        Matcher matcher = priorityPattern.matcher(messageText);
        if(matcher.matches()){
            return Integer.parseInt(matcher.group(1));
        }
        return defaultValue;
    }

    public void save(Todo todo){
        todos.put(todo.id,todo);
        repo.save(todo);
    }

    private Optional<SymUser> getAssignee(SymMessage message) {
        String autoActor = findAutoActorAssignee(message.getMessageText());
        if(autoActor != null){
            SymUser user = new SymUser();
            user.setId(message.getSymUser().getId());
            user.setDisplayName(autoActor);
            return Optional.of(user);
        }

        if(message.getStream().getStreamType() != SymStreamTypes.Type.ROOM){
            return Optional.of(message.getSymUser());
        }
        List<SymUser> mentions = EntityDataHelper.getUserMentions(message.getEntityData(), symClient);
        if(mentions.size() >=1){
            return Optional.of(mentions.get(0));
        }
        return Optional.empty();
    }

    private String findAutoActorAssignee(String messageText) {
        Pattern pattern = Pattern.compile(".*@(\\S+).*");
        Matcher match = pattern.matcher(messageText);
        if(match.matches()){
            String actor =  match.group(1);
            if(this.actorService.actors.keySet().contains(actor)){
                return actor;
            }
        }
        return null;
    }

    public List<Todo> getForRoom(String roomId) {
        return filtered(t->t.roomId.equals(roomId) && t.status != Status.DONE);
    }

    public List<Todo> getForAssignee(Long id) {
        return filtered(t->id.equals(t.assigneeId) && t.status != Status.DONE);
    }

    public List<Todo> filtered(Predicate<Todo> predicate){
        return todos.values().stream().filter(predicate).collect(Collectors.toList());
    }

    public Todo assignTask(SymMessage message) {
        long id = getTaskIdFromMessage(message.getMessageText());
        Todo todo = todos.get(id);
        Optional<SymUser> assignee = getAssignee(message);
        if(!assignee.isPresent()){
            return null;
        }

        SymUser symUser = assignee.get();
        todo.assigneeName = symUser.getDisplayName();
        todo.assigneeId = symUser.getId();
        save(todo);
        return todo;
    }

    public Todo startTask(SymMessage message) {
        long id = getTaskIdFromMessage(message.getMessageText());
        Todo todo = todos.get(id);
        if(todo.status ==Status.WIP){
            return null;
        }
        Optional<SymUser> assignee = getAssignee(message);
        if(!assignee.isPresent()){
            if(todo.assigneeId == null){
                todo.assigneeId = message.getSymUser().getId();
                todo.assigneeName = message.getSymUser().getDisplayName();
            }
        } else {
            SymUser symUser = assignee.get();
            todo.assigneeName = symUser.getDisplayName();
            todo.assigneeId = symUser.getId();
        }
        todo.status = Status.WIP;
        save(todo);
        return todo;
    }
    public Todo completeTask(SymMessage message) {
        long id = getTaskIdFromMessage(message.getMessageText());
        Todo todo = todos.get(id);
        if(todo.status ==Status.DONE){
            return null;
        }
        todo.status = Status.DONE;
        save(todo);
        return todo;
    }

    public long getTaskIdFromMessage(String message){
        Matcher matcher = taskIdPattern.matcher(message);
        matcher.matches();
        String id = matcher.group(1);
        return Long.parseLong(id);
    }

    public Todo editTask(SymMessage message) {

        long id = getTaskIdFromMessage(message.getMessageText());
        String messageText = message.getMessageText().replaceFirst("#task-edit (TODO-|TASK-)?\\d+", "");
        Todo oldTodo = todos.get(id);
        Optional<SymUser> assignee = getAssignee(message);
        String assigneeName = assignee.map(SymUser::getDisplayName).orElse(oldTodo.assigneeName);
        Long assigneeId = assignee.map(SymUser::getId).orElse(oldTodo.assigneeId);
        List<String> labels = getLabels(messageText);
        if(labels.size() == 0){
            labels = oldTodo.labels;
        }
        int priority = getPriority(messageText, oldTodo.priority);
        for(String label : labels){
            messageText = messageText.replace("#" + label,"");
        }


        DueDateMatch dueDateMatch = getDueDate(messageText);
        LocalDateTime due = dueDateMatch.due.get(0);
        if(due == null){
            due = oldTodo.due;
        }
        String summary = messageText
                .replace("@" + assigneeName, "")
                .replace("p:" + priority, "")
                .replaceFirst("(at|in|by)\\s+" + dueDateMatch.matchedText + ".*[^#@]", "");
        if(summary.trim().isEmpty()){
            summary = oldTodo.summary;
        }
        Todo todo = new Todo(
                id,
                summary,
                oldTodo.creatorName,
                oldTodo.creatorId,
                assigneeName,
                assigneeId,
                oldTodo.roomId,
                oldTodo.roomName,
                due,
                priority,
                labels);
        save(todo);

        return todo;
    }


    public static class DueDateMatch {
        public List<LocalDateTime> due;
        public String matchedText;

        public DueDateMatch(List<LocalDateTime> due, String matchedText) {
            this.due = due;
            this.matchedText = matchedText;
        }
    }

    private DueDateMatch getDueDate(String text){
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(text);
        if(groups.size() > 0){

            DateGroup dateGroup = groups.get(0);
            logger.info("Due Date Parsing Matched text: {}", dateGroup.getFullText());
            List<Date> dates = dateGroup.getDates();
            if(dates.size() > 0){
                LocalDateTime due = dates.get(0).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                return new DueDateMatch(Lists.newArrayList(due), dateGroup.getText());
            }
        }
        return new DueDateMatch(Lists.newArrayList((LocalDateTime)null), "zzz");
    }


    private DueDateMatch getDueDates(String expression){
        Parser parser = new Parser();
        List<Date> dates = new ArrayList<>();
        List<DateGroup> dateGroups = parser.parse(expression);

        if(dateGroups.size() == 0){
            return new DueDateMatch(Lists.newArrayList(),"");
        }

        DateGroup dateGroup = dateGroups.get(0);
        dates.addAll(dateGroup.getDates());
        logger.info("Due Date Parsing Matched text: {}", dateGroup.getFullText());

        if(!dateGroup.isRecurring() || dateGroup.getRecursUntil() == null){
            return new DueDateMatch(
                    dates.stream().map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() ).collect(Collectors.toList()),
                    dateGroup.getText()
            );
        }

        Date recurrsUntil = dateGroup.getRecursUntil();
        Date maxDate = dates.get(dates.size()-1);

        while (maxDate.before(recurrsUntil)){
            dateGroup = parser.parse(expression, maxDate).get(0);
            dates.addAll(dateGroup.getDates());
            maxDate = dates.get(dates.size()-1);
        }

        return new DueDateMatch(
                dates.stream().filter(d->d.before(recurrsUntil)).map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() ).collect(Collectors.toList()),
                dateGroup.getText()
        );
    }

    private List<String> getLabels(String text){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("#[^#\\s]+");
        Matcher matcher = pattern.matcher(text);
        while(matcher.find()){
            result.add(matcher.group().replaceFirst("#", ""));
        }
        return result;
    }

    public Map<String, Long> getCountOfOpenTasksByAssignee(String roomId){
        Map<String, Long> countByUser = filtered(t->t.status != Status.DONE)
                .stream()
                .filter(t->roomId.isEmpty() || t.roomId.equals(roomId))
                .map(t -> t.assigneeName)
                .collect(groupingBy(Function.identity(), Collectors.counting()));
        return countByUser;
    }

    public Map<String, Long> getCountOfClosedTasksByAssignee(String roomId){
        Map<String, Long> countByUser = filtered(t->t.status == Status.DONE)
                .stream()
                .filter(t->roomId.isEmpty() || t.roomId.equals(roomId))
                .map(t -> t.assigneeName)
                .collect(groupingBy(Function.identity(), Collectors.counting()));
        return countByUser;
    }

}
