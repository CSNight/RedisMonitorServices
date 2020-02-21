package csnight.redis.monitor.busi.sys.exp;

import csnight.redis.monitor.db.blurry.Query;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MailRecQueryExp {
    @Query(type = Query.Type.IN, propName = "id")
    private Set<String> ids = new HashSet<>();
    @Query(blurry = "tos")
    private String receiver;
    @Query(type = Query.Type.GREATER_THAN, propName = "ct")
    private Date std;
    @Query(type = Query.Type.LESS_THAN, propName = "ct")
    private Date etd;

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public Date getStd() {
        return std;
    }

    public void setStd(Date std) {
        this.std = std;
    }

    public Date getEtd() {
        return etd;
    }

    public void setEtd(Date etd) {
        this.etd = etd;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
