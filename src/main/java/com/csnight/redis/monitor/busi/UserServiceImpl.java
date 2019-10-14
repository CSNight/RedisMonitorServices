package com.csnight.redis.monitor.busi;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.db.jpa.SysOrg;
import com.csnight.redis.monitor.db.repos.SysOrgRepository;
import com.csnight.redis.monitor.utils.JSONUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl {
    private SysOrgRepository sysOrgRepository;

    public UserServiceImpl(SysOrgRepository sysOrgRepository) {
        this.sysOrgRepository = sysOrgRepository;
    }

    public String GetOrgTree() {
        Optional<SysOrg> sysOrg = sysOrgRepository.findById("1");
        return sysOrg.map(JSONUtil::pojo2json).orElse("");
    }

    public String GetOrgByPid(String pid) {
        List<SysOrg> orgList = sysOrgRepository.findByPid(pid);
        return JSONUtil.object2json(orgList);
    }

    public String ModifyOrg(JSONObject jo_org) {
        SysOrg sysOrg = new SysOrg();
        sysOrg.setId(jo_org.getString("id"));
        sysOrg.setEnabled(jo_org.getBoolean("enabled"));
        sysOrg.setPid(jo_org.getString("pid"));
        sysOrg.setName(jo_org.getString("name"));
        SysOrg res = sysOrgRepository.save(sysOrg);
        return JSONUtil.pojo2json(res);
    }

    public String DeleteOrgById(String id) {
        Optional<SysOrg> sysOrgOpt = sysOrgRepository.findById(id);
        if (sysOrgOpt.isPresent()) {
            SysOrg sysOrg = sysOrgOpt.get();
            if (sysOrg.getChildren().size() > 0) {
                Set<SysOrg> ids = new HashSet<>();
                getOrgChildIds(sysOrg, ids);
                sysOrgRepository.deleteInBatch(ids);
            } else {
                sysOrgRepository.deleteById(id);
            }
            return "success";
        }
        return "failed";
    }

    private void getOrgChildIds(SysOrg sysOrg, Set<SysOrg> ids) {
        for (SysOrg child : sysOrg.getChildren()) {
            ids.add(child);
            if (child.getChildren().size() > 0) {
                getOrgChildIds(child, ids);
            }
        }
    }
}
