package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsShakeRepository;
import csnight.redis.monitor.redis.data.ShakeConfGenerator;
import csnight.redis.monitor.rest.rms.dto.DumpDto;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author csnight
 * @description
 * @since 2020-1-18 21:17
 */
@Service
public class RmsDataDumpImpl {
    @Resource
    private RmsShakeRepository shakeRepository;
    @Resource
    private RmsInsRepository insRepository;

    public List<RmsShakeRecord> GetAll() {
        return shakeRepository.findAll();
    }

    public List<RmsShakeRecord> GetByUser() {
        return shakeRepository.findByCreateUser(BaseUtils.GetUserFromContext());
    }

    public RmsShakeRecord NewShakeConf(DumpDto dto) throws IOException {
        RmsShakeRecord shakeRecord = new RmsShakeRecord();
        JSONObject joConfig = JSONObject.parseObject(dto.getConfigs());
        shakeRecord.setSource_ins(joConfig.getString("sourceId"));
        shakeRecord.setTarget_ins(joConfig.getString("targetId"));
        String type = joConfig.getString("mode");
        switch (dto.getType()) {
            case "dump":
            case "rump": {
                boolean ins_source = insRepository.existsById(shakeRecord.getSource_ins());
                if (!ins_source) {
                    return null;
                }
                break;
            }
            case "restore": {
                boolean ins_target = insRepository.existsById(shakeRecord.getTarget_ins());
                if (!ins_target) {
                    return null;
                }
                break;
            }
            case "sync": {
                boolean ins_source = insRepository.existsById(shakeRecord.getSource_ins());
                boolean ins_target = insRepository.existsById(shakeRecord.getTarget_ins());
                if (!ins_source || !ins_target) {
                    return null;
                }
                break;
            }
        }
        ShakeConfGenerator confGenerator = new ShakeConfGenerator();
        String file = confGenerator.GenerateFile(joConfig);
        if (file == null) {
            return null;
        }
        shakeRecord.setFilepath(file);
        shakeRecord.setShake_type(dto.getType());
        shakeRecord.setConf(dto.getConfigs().replaceAll(" ", ""));
        shakeRecord.setCreate_time(new Date());
        shakeRecord.setCreate_user(BaseUtils.GetUserFromContext());
        return shakeRepository.save(shakeRecord);
    }

    public String DeleteRecord(String cid) {
        Optional<RmsShakeRecord> shakeRecord = shakeRepository.findById(cid);
        if (shakeRecord.isPresent()) {
            ShakeConfGenerator.clearConf(shakeRecord.get().getFilepath());
            shakeRepository.deleteById(cid);
            return "success";
        }
        return "failed";
    }
}
