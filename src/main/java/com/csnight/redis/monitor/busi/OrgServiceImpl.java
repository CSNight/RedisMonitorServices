package com.csnight.redis.monitor.busi;

import com.alibaba.fastjson.JSONObject;
import com.csnight.redis.monitor.db.jpa.SysOrg;
import com.csnight.redis.monitor.db.repos.SysOrgRepository;
import com.csnight.redis.monitor.utils.JSONUtil;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrgServiceImpl {
    private SysOrgRepository sysOrgRepository;

    public OrgServiceImpl(SysOrgRepository sysOrgRepository) {
        this.sysOrgRepository = sysOrgRepository;
    }

    public String GetOrgTree() {
        Optional<SysOrg> sysOrg = sysOrgRepository.findById(1L);
        return sysOrg.map(JSONUtil::pojo2json).orElse("");
    }

    public String GetOrgList() {
        List<SysOrg> orgList = sysOrgRepository.findAll();
        for (SysOrg sysOrg : orgList) {
            sysOrg.setChildren(null);
        }
        return JSONUtil.object2json(orgList);
    }

    public String GetOrgByPid(String pid) {
        List<SysOrg> orgList = sysOrgRepository.findByPid(Long.parseLong(pid));
        return JSONUtil.object2json(orgList);
    }

    public String ModifyOrg(JSONObject jo_org) {
        if (jo_org.containsKey("id")) {
            Optional<SysOrg> sysOrg = sysOrgRepository.findById(jo_org.getLong("id"));
            if (sysOrg.isPresent()) {
                SysOrg old_org = sysOrg.get();
                boolean enabled = old_org.isEnabled();
                old_org.setEnabled(jo_org.containsKey("enabled") ? jo_org.getBoolean("enabled") : old_org.isEnabled());
                old_org.setPid(jo_org.containsKey("pid") ? jo_org.getLong("pid") : old_org.getPid());
                old_org.setName(jo_org.containsKey("name") ? jo_org.getString("name") : old_org.getName());
                SysOrg res = sysOrgRepository.save(old_org);
                if (old_org.isEnabled() != enabled) {
                    Set<SysOrg> ids = new HashSet<>();
                    getOrgChildIds(old_org, ids);
                    for (SysOrg child : ids) {
                        child.setEnabled(res.isEnabled());
                        sysOrgRepository.save(child);
                    }
                }
                ModifyParent(res);
                return JSONUtil.pojo2json(res);
            }
        }
        return "failed";
    }

    public String NewOrg(JSONObject jo_org, String user) {
        if (jo_org.containsKey("pid")) {
            SysOrg sysOrg = new SysOrg();
            sysOrg.setEnabled(jo_org.containsKey("enabled") ? jo_org.getBoolean("enabled") : false);
            sysOrg.setPid(jo_org.getLong("pid"));
            sysOrg.setName(jo_org.containsKey("name") ? jo_org.getString("name") : "unknown");
            sysOrg.setCreate_time(new Date());
            sysOrg.setCreate_user(user);
            SysOrg res = sysOrgRepository.save(sysOrg);
            return JSONUtil.pojo2json(res);
        }
        return "failed";
    }

    public String DeleteOrgById(String id) {
        Optional<SysOrg> sysOrgOpt = sysOrgRepository.findById(Long.parseLong(id));
        if (sysOrgOpt.isPresent()) {
            SysOrg sysOrg = sysOrgOpt.get();
            if (sysOrg.getChildren().size() > 0) {
                Set<SysOrg> ids = new HashSet<>();
                getOrgChildIds(sysOrg, ids);
                sysOrgRepository.deleteInBatch(ids);
            }
            sysOrgRepository.deleteById(Long.parseLong(id));
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

    private void ModifyParent(SysOrg current) {
        if (current.getPid() == 1) {
            return;
        }
        List<Boolean> enables = sysOrgRepository.findEnabledByPid(current.getPid());
        Optional<SysOrg> top_parent_option = sysOrgRepository.findById(current.getPid());
        if (top_parent_option.isPresent() && any(enables)) {
            SysOrg top_parent = top_parent_option.get();
            top_parent.setEnabled(current.isEnabled());
            SysOrg top_modify = sysOrgRepository.save(top_parent);
            ModifyParent(top_modify);
        }
    }

    private boolean any(List<Boolean> list) {
        if (list.isEmpty()) {
            return true;
        }
        boolean isSame = true;
        boolean temp_val = list.get(0);
        for (Boolean b : list) {
            if (temp_val != b) {
                isSame = false;
                break;
            }
        }
        return isSame;
    }
}
