import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Natty {
    Parser p = new Parser();

    @Test
    public void name() throws Exception {
        System.out.println(parseRecurringDates("SCC every friday until three tuesdays from now"));
        System.out.println(parseRecurringDates("every mon and tues at 1500 until 6 months"));
        //System.out.println(parseRecurringDates("every 2 fridays until Jan 2019"));
    }

    private List<Date> parseRecurringDates(String expression){

        List<Date> dates = new ArrayList<>();
        List<DateGroup> dateGroups = p.parse(expression);

        if(dateGroups.size() == 0){
            return dates;
        }

        DateGroup dateGroup = dateGroups.get(0);
        dates.addAll(dateGroup.getDates());

        if(!dateGroup.isRecurring() || dateGroup.getRecursUntil() == null){
            return dates;
        }

        Date recurrsUntil = dateGroup.getRecursUntil();
        Date maxDate = dates.get(dates.size()-1);

        while (maxDate.before(recurrsUntil)){
            dateGroup = p.parse(expression, maxDate).get(0);
            dates.addAll(dateGroup.getDates());
            maxDate = dates.get(dates.size()-1);
        }
        return dates.stream().filter(d->d.before(recurrsUntil)).collect(Collectors.toList());
    }
}
