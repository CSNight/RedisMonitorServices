package csnight.redis.monitor.busi.rms;

import csnight.redis.monitor.db.jpa.RmsIndicator;
import csnight.redis.monitor.db.repos.RmsIndicatorRepository;
import csnight.redis.monitor.rest.rms.dto.IndicatorDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class RmsIndicatorImpl {
    @Resource
    private RmsIndicatorRepository indicatorRepository;

    public List<RmsIndicator> GetIndicators() {
        return indicatorRepository.findAll();
    }

    public RmsIndicator AddIndicator(IndicatorDto dto) {
        RmsIndicator indicator = new RmsIndicator();
        indicator.setName(dto.getName());
        indicator.setLabel(dto.getLabel());
        indicator.setExp_support(dto.getExp_support());
        indicator.setSign_support(dto.getSign_support());
        indicator.setUnit(dto.getUnit());
        indicator.setCreate_time(new Date());
        if (checkConflict(indicator, true)) {
            return indicatorRepository.save(indicator);
        }
        return null;
    }

    public RmsIndicator UpdateIndicator(IndicatorDto dto) {
        RmsIndicator indicator = indicatorRepository.findOnly(dto.getId());
        if (indicator != null) {
            indicator.setName(dto.getName());
            indicator.setLabel(dto.getLabel());
            indicator.setExp_support(dto.getExp_support());
            indicator.setSign_support(dto.getSign_support());
            indicator.setUnit(dto.getUnit());
            if (!indicator.getName().equals(dto.getName()) || !indicator.getLabel().equals(dto.getLabel())) {
                if (checkConflict(indicator, false)) {
                    return indicatorRepository.save(indicator);
                }
            } else {
                return indicatorRepository.save(indicator);
            }
        }
        return null;
    }

    public String DeleteIndicator(String id) {
        if (indicatorRepository.existsById(id)) {
            indicatorRepository.deleteById(id);
            return "success";
        }
        return "failed";
    }

    private boolean checkConflict(RmsIndicator indicator, boolean isNew) {
        boolean isValid = true;
        RmsIndicator existName = indicatorRepository.findByName(indicator.getName());
        RmsIndicator existLabel = indicatorRepository.findByLabel(indicator.getLabel());
        if (isNew) {
            if (existName != null || existLabel != null) {
                isValid = false;
            }
        } else {
            if (existName != null && existName.getId().equals(indicator.getId())) {
                isValid = false;
            }
            if (existLabel != null && existLabel.getId().equals(indicator.getId())) {
                isValid = false;
            }
        }
        return isValid;
    }
}
