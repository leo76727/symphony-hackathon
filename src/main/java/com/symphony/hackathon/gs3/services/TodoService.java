package com.symphony.hackathon.gs3.services;

import com.google.common.collect.Lists;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import com.symphony.hackathon.gs3.bot.TodoBot;
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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TodoService {
    private static final Logger logger = LoggerFactory.getLogger(TodoService.class);
    private final AtomicLong idGenerator = new AtomicLong(1);

    private SymphonyClient symClient;
    private Map<Long, Todo> todos = new HashMap<>();
    private TodoRepo repo = new TodoRepo();

    public TodoService(SymphonyClient symClient){
        this.symClient = symClient;
        todos = repo.loadAll();
        idGenerator.set(todos.keySet().stream().mapToLong(l->l).max().orElse(0) + 1 );
    }
    public Todo createTask(SymMessage message) throws StreamsException {
        return createTasks(message, false).get(0);
    }
    public List<Todo> createTasks(SymMessage message) throws StreamsException {
        return createTasks(message, true);
    }

    public List<Todo> createTasks(SymMessage message, boolean recurring) throws StreamsException {

        List<Todo> result = new ArrayList<>();

        String messageText = message.getMessageText();
        Optional<SymUser> assignee = getAssignee(message);
        String assigneeName = assignee.map(SymUser::getDisplayName).orElse("Unassigned");
        Long assigneeId = assignee.map(SymUser::getId).orElse(null);
        List<String> labels = getLabels(messageText);
        System.out.println(labels);
        for(String label : labels){
            messageText = messageText.replace("#" + label,"");
        }
        String summary = messageText
                .replace("/task ", "")
                .replace("/taskr ", "")
                .replace("@" + assigneeName, "");

        List<LocalDateTime> dueDates;
        if(recurring){
            dueDates = getDueDates(messageText);
        } else {
            dueDates = Lists.newArrayList(getDueDate(messageText));
        }

        for(LocalDateTime due : dueDates){
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
                    labels
            );
            save(todo);
            result.add(todo);
        }

        return result;
    }

    public void save(Todo todo){
        todos.put(todo.id,todo);
        repo.save(todo);
    }

    private Optional<SymUser> getAssignee(SymMessage message) {
        if(message.getStream().getStreamType() != SymStreamTypes.Type.ROOM){
            return Optional.of(message.getSymUser());
        }
        List<SymUser> mentions = EntityDataHelper.getUserMentions(message.getEntityData(), symClient);
        if(mentions.size() >=1){
            return Optional.of(mentions.get(0));
        }
        return Optional.empty();
    }

    public List<Todo> getForRoom(String roomId) {
        return filtered(t->t.roomId.equals(roomId));
    }

    public List<Todo> getForAssignee(Long id) {
        return filtered(t->id.equals(t.assigneeId));
    }

    public List<Todo> filtered(Predicate<Todo> predicate){
        return todos.values().stream().filter(predicate).collect(Collectors.toList());
    }

    public static class DueDateMatch {
        public LocalDateTime due;
        public String matchedText;

        public DueDateMatch(LocalDateTime due, String matchedText) {
            this.due = due;
            this.matchedText = matchedText;
        }
    }

    private LocalDateTime getDueDate(String text){
        Parser parser = new Parser();
        List<DateGroup> groups = parser.parse(text);
        if(groups.size() > 0){

            DateGroup dateGroup = groups.get(0);
            logger.info("Due Date Parsing Matched text: {}", dateGroup.getFullText());
            List<Date> dates = dateGroup.getDates();
            if(dates.size() > 0){
                return dates.get(0).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
        }
        return null;
    }

    private List<LocalDateTime> getDueDates(String text){
        return parseRecurringDates(text).stream().map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() ).collect(Collectors.toList());
    }


    private List<Date> parseRecurringDates(String expression){
        Parser parser = new Parser();
        List<Date> dates = new ArrayList<>();
        List<DateGroup> dateGroups = parser.parse(expression);

        if(dateGroups.size() == 0){
            return dates;
        }

        DateGroup dateGroup = dateGroups.get(0);
        dates.addAll(dateGroup.getDates());
        logger.info("Due Date Parsing Matched text: {}", dateGroup.getFullText());

        if(!dateGroup.isRecurring() || dateGroup.getRecursUntil() == null){
            return dates;
        }

        Date recurrsUntil = dateGroup.getRecursUntil();
        Date maxDate = dates.get(dates.size()-1);

        while (maxDate.before(recurrsUntil)){
            dateGroup = parser.parse(expression, maxDate).get(0);
            dates.addAll(dateGroup.getDates());
            maxDate = dates.get(dates.size()-1);
        }
        return dates.stream().filter(d->d.before(recurrsUntil)).collect(Collectors.toList());
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

}
