package testhelpers;

import com.github.ladderwinner.QueryParams;
import com.github.ladderwinner.TraceMe;

import java.util.HashMap;


public class QueryHashMap extends HashMap<String, String> {

    public QueryHashMap(TraceMe traceMe) {
        super(traceMe.toMap());
    }

    public String get(QueryParams key) {
        return get(key.toString());
    }

    public boolean containsKey(QueryParams key) {
        return super.containsKey(key.toString());
    }
}
