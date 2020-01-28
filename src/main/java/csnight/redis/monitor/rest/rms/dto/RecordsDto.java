package csnight.redis.monitor.rest.rms.dto;

import java.util.HashSet;
import java.util.Set;

public class RecordsDto {
    private Set<String> ids = new HashSet<>();

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }
}
