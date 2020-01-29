package csnight.redis.monitor.redis.statistic;

import com.csnight.jedisql.JediSQL;
import csnight.redis.monitor.db.repos.RmsRpsRepository;
import csnight.redis.monitor.redis.pool.RedisPoolInstance;
import csnight.redis.monitor.utils.IdentifyUtils;
import csnight.redis.monitor.utils.ReflectUtils;

public class PhysicalStatistic {
    private RedisPoolInstance pool;
    private String jid = IdentifyUtils.getUUID();
    private JediSQL jediSQL;
    private String ins_id;
    private RmsRpsRepository rpsRepository;

    public PhysicalStatistic(RedisPoolInstance pool) {
        this.pool = pool;
        this.rpsRepository = ReflectUtils.getBean(RmsRpsRepository.class);
    }

    public boolean initialize() {
        boolean success = true;
        try {
            if (pool != null) {
                jediSQL = pool.getJedis(jid);
                ins_id = pool.getId();
            } else {
                success = false;
            }
        } catch (Exception ex) {
            success = false;
        }
        return success;
    }

    public void destory() {
        pool.close(jid);
    }
}
