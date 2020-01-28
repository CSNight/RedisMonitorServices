package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import csnight.redis.monitor.db.jpa.RmsShakeRecord;
import csnight.redis.monitor.db.repos.RmsInsRepository;
import csnight.redis.monitor.db.repos.RmsShakeRepository;
import csnight.redis.monitor.redis.data.ShakeConfGenerator;
import csnight.redis.monitor.rest.rms.dto.RecordsDto;
import csnight.redis.monitor.rest.rms.dto.ShakeConfDto;
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
    @Resource
    private RmsDataBackupImpl rmsDataBackup;

    public List<RmsShakeRecord> GetAll() {
        return shakeRepository.findAll();
    }

    public List<RmsShakeRecord> GetByUser() {
        return shakeRepository.findByCreateUser(BaseUtils.GetUserFromContext());
    }

    public RmsShakeRecord NewShakeConf(ShakeConfDto dto) throws IOException {
        RmsShakeRecord shakeRecord = new RmsShakeRecord();
        JSONObject joConfig = JSONObject.parseObject(dto.getConfigs());
        switch (dto.getType()) {
            case "dump":
                boolean ins_source = insRepository.existsById(joConfig.getString("sourceId"));
                if (!ins_source) {
                    return null;
                }
                String output = generateOutput(dto.getType());
                JSONPath.set(joConfig, "$.target.rdb.output", output);
                shakeRecord.setSource_ins(joConfig.getString("sourceId"));
                break;
            case "decode":
                String outputDecode = generateOutput(dto.getType());
                JSONPath.set(joConfig, "$.target.rdb.output", outputDecode);
                break;
            case "restore":
                boolean ins_target = insRepository.existsById(joConfig.getString("targetId"));
                if (!ins_target) {
                    return null;
                }
                shakeRecord.setTarget_ins(joConfig.getString("targetId"));
                break;
            case "rump":
            case "sync":
                boolean syn_source = insRepository.existsById(joConfig.getString("sourceId"));
                boolean syn_target = insRepository.existsById(joConfig.getString("targetId"));
                if (!syn_source || !syn_target) {
                    return null;
                }
                shakeRecord.setSource_ins(joConfig.getString("sourceId"));
                shakeRecord.setTarget_ins(joConfig.getString("targetId"));
                break;
        }
        ShakeConfGenerator confGenerator = new ShakeConfGenerator();
        String file = confGenerator.GenerateFile(joConfig);
        if (file == null) {
            return null;
        }
        shakeRecord.setFilepath(file);
        shakeRecord.setShake_type(dto.getType());
        shakeRecord.setConf(joConfig.toJSONString());
        shakeRecord.setCreate_time(new Date());
        shakeRecord.setCreate_user(BaseUtils.GetUserFromContext());
        return shakeRepository.save(shakeRecord);
    }

    public String DeleteRecord(String cid) {
        Optional<RmsShakeRecord> optShakeRecord = shakeRepository.findById(cid);
        if (optShakeRecord.isPresent()) {
            RmsShakeRecord shakeRecord = optShakeRecord.get();
            ShakeConfGenerator.clearConf(shakeRecord.getFilepath());
            if (shakeRecord.getRelate_backup() != null) {
                rmsDataBackup.DeleteById(shakeRecord.getRelate_backup());
            }
            shakeRepository.deleteById(cid);
            return "success";
        }
        return "failed";
    }

    public String ClearShakeRecords() {
        shakeRepository.deleteAll();
        rmsDataBackup.ClearRecord();
        return "success";
    }

    public String DeleteMultiRecords(RecordsDto dto) {
        for (String id : dto.getIds()) {
            DeleteRecord(id);
        }
        return "success";
    }

    private String generateOutput(String mode) {
        if (mode.equals("dump")) {
            return mode + "_" + System.nanoTime() + ".rdb";
        } else {
            return mode + "_" + System.nanoTime() + ".json";
        }
    }
}
