package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import csnight.redis.monitor.db.repos.RmsShakeRepository;
import csnight.redis.monitor.rest.rms.dto.DumpDto;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:17
 */
@Service
public class RmsDataDumpImpl {
    @Resource
    private RmsShakeRepository shakeRepository;

    public RmsShakeRecord NewShakeConf(DumpDto dto) {
        RmsShakeRecord shakeRecord = new RmsShakeRecord();
        shakeRecord.setCreate_time(new Date());
        shakeRecord.setCreate_user(BaseUtils.GetUserFromContext());
        JSONObject joConfig = JSONObject.parseObject(dto.getConfigs());
        shakeRecord.setSource_ins(joConfig.getString("sourceId"));
        shakeRecord.setTarget_ins(joConfig.getString("targetId"));
        return shakeRecord;
    }

}
