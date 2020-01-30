package csnight.redis.monitor.redis.statistic;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.repos.RmsRpsRepository;
import csnight.redis.monitor.utils.ReflectUtils;

public class PhysicalStatistic {
    private String appId;
    private JediSQL jediSQL;
    private String ins_id;
    private RmsRpsRepository rpsRepository;


    public PhysicalStatistic(JediSQL jediSQL, String ins_id, String appId) {
        this.jediSQL = jediSQL;
        this.ins_id = ins_id;
        this.appId = appId;
        this.rpsRepository = ReflectUtils.getBean(RmsRpsRepository.class);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIns_id() {
        return ins_id;
    }


    public void execute() {

    }
}
