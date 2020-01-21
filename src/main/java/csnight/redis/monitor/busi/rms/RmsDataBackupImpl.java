package csnight.redis.monitor.busi.rms;

import csnight.redis.monitor.db.jpa.RmsDataRecord;
import csnight.redis.monitor.db.repos.RmsDataRecRepository;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.YamlUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:43
 */
@Service
public class RmsDataBackupImpl {
    @Resource
    private RmsDataRecRepository dataRecRepository;
    private String recordDir = System.getProperty("user.dir") + "/" + YamlUtils.getStrYmlVal("dumpdir.record-dir") + "/";

    public List<RmsDataRecord> GetDataRecord() {
        return dataRecRepository.findByCreateUser(BaseUtils.GetUserFromContext());
    }

    public List<RmsDataRecord> GetAllDataRecord() {
        return dataRecRepository.findAll();
    }

    public RmsDataRecord GetDataRecordById(String id) {
        return dataRecRepository.findById(id).orElse(null);
    }

    private String DeleteById(String id) {
        boolean delSuccess = false;
        RmsDataRecord dataRecord = dataRecRepository.getOne(id);
        if (dataRecord != null) {
            String dataFilePath = recordDir + dataRecord.getFilename();
            File f = new File(dataFilePath);
            if (f.exists()) {
                delSuccess = f.delete();
            }
            dataRecRepository.deleteById(id);
            return delSuccess ? "success" : "failed";
        }
        return "failed";
    }
}
