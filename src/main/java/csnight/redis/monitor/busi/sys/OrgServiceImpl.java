package csnight.redis.monitor.busi.sys;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.busi.sys.exp.OrgQueryExp;
import csnight.redis.monitor.db.blurry.QueryAnnotationProcess;
import csnight.redis.monitor.db.jpa.SysOrg;
import csnight.redis.monitor.db.jpa.SysUser;
import csnight.redis.monitor.db.repos.SysOrgRepository;
import csnight.redis.monitor.db.repos.SysUserRepository;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.utils.BaseUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrgServiceImpl {
    private SysOrgRepository sysOrgRepository;
    @Resource
    private SysUserRepository userRepository;

    public OrgServiceImpl(SysOrgRepository sysOrgRepository) {
        this.sysOrgRepository = sysOrgRepository;
    }

    /**
     * 功能描述: 查询部门树
     *
     * @return : csnight.redis.monitor.db.jpa.SysOrg
     * @author chens
     * @since 2019/12/26 10:37
     */
    @Cacheable(value = "orgTree")
    public SysOrg GetOrgTree() {
        return sysOrgRepository.findOnly(1L);
    }

    /**
     * 功能描述: 查询部门列表，无子项
     *
     * @return : java.util.List<csnight.redis.monitor.db.jpa.SysOrg>
     * @author chens
     * @since 2019/12/26 10:37
     */
    @Cacheable(value = "orgList")
    public List<SysOrg> GetOrgList() {
        List<SysOrg> orgList = sysOrgRepository.findAll();
        for (SysOrg sysOrg : orgList) {
            sysOrg.setChildren(null);
        }
        return orgList;
    }

    /**
     * 功能描述: 部门查询
     *
     * @param exp 查询条件
     * @return : java.util.List<csnight.redis.monitor.db.jpa.SysOrg>
     * @author chens
     * @since 2019/12/26 10:37
     */
    public List<SysOrg> QueryBy(OrgQueryExp exp) {
        List<SysOrg> orgList = sysOrgRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (SysOrg sysOrg : orgList) {
            sysOrg.setChildren(null);
        }
        return orgList;
    }

    /**
     * 功能描述: 查询子部门
     *
     * @param pid 部门id
     * @return : java.util.List<csnight.redis.monitor.db.jpa.SysOrg>
     * @author chens
     * @since 2019/12/26 10:38
     */
    public List<SysOrg> GetOrgByPid(String pid) {
        return sysOrgRepository.findByPid(Long.parseLong(pid));
    }

    /**
     * 功能描述: 根据id及状态查询部门
     *
     * @param id      部门id
     * @param enabled 部门状态
     * @return : csnight.redis.monitor.db.jpa.SysOrg
     * @author chens
     * @since 2019/12/26 10:38
     */
    public SysOrg GetOrgByIdAndEnabled(String id, boolean enabled) {
        SysOrg org_old = sysOrgRepository.findOnly(Long.parseLong(id));
        SysOrg copy_org = new SysOrg();
        if (org_old != null) {
            copy_org = JSONObject.parseObject(JSONObject.toJSONString(org_old), SysOrg.class);
            getOrgChildFilter(org_old, copy_org, enabled);
        }
        return copy_org;
    }

    /**
     * 功能描述:修改部门
     *
     * @param jo_org 部门实例
     * @return : csnight.redis.monitor.db.jpa.SysOrg
     * @author chens
     * @since 2019/12/26 10:38
     */
    @CacheEvict(value = {"orgTree", "orgList"}, beforeInvocation = true, allEntries = true)
    public SysOrg ModifyOrg(JSONObject jo_org) throws ConflictsException {
        if (jo_org.containsKey("id")) {
            SysOrg old_org = sysOrgRepository.findOnly(jo_org.getLong("id"));
            if (old_org != null) {
                //屏蔽自归属错误
                if (jo_org.getLong("pid").equals(old_org.getId())) {
                    throw new ConflictsException("Organization can not set parent to it self!");
                }
                boolean enabled = old_org.isEnabled();
                old_org.setEnabled(jo_org.containsKey("enabled") ? jo_org.getBoolean("enabled") : old_org.isEnabled());
                old_org.setPid(jo_org.containsKey("pid") ? jo_org.getLong("pid") : old_org.getPid());
                old_org.setName(jo_org.containsKey("name") ? jo_org.getString("name") : old_org.getName());
                if (checkOrgConflict(old_org, false)) {
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
                    return res;
                } else {
                    throw new ConflictsException("Department with same name already exists!");
                }
            }
        }
        return null;
    }

    /**
     * 功能描述: 新增部门
     *
     * @param jo_org 部门实例
     * @param user   用户
     * @return : csnight.redis.monitor.db.jpa.SysOrg
     * @author chens
     * @since 2019/12/26 10:38
     */
    @CacheEvict(value = {"orgTree", "orgList"}, beforeInvocation = true, allEntries = true)
    public SysOrg NewOrg(JSONObject jo_org, String user) throws ConflictsException {
        if (jo_org.containsKey("pid")) {
            SysOrg sysOrg = new SysOrg();
            sysOrg.setEnabled(jo_org.containsKey("enabled") ? jo_org.getBoolean("enabled") : false);
            sysOrg.setPid(jo_org.getLong("pid"));
            sysOrg.setName(jo_org.containsKey("name") ? jo_org.getString("name") : "unknown");
            sysOrg.setCreate_time(new Date());
            sysOrg.setCreate_user(user);
            if (checkOrgConflict(sysOrg, true)) {
                return sysOrgRepository.save(sysOrg);
            } else {
                throw new ConflictsException("Department with same name already exists!");
            }
        }
        return null;
    }

    /**
     * 功能描述: 删除部门
     *
     * @param id 部门id
     * @return : java.lang.String
     * @author chens
     * @since 2019/12/26 10:38
     */
    @CacheEvict(value = {"orgTree", "orgList"}, beforeInvocation = true, allEntries = true)
    public String DeleteOrgById(String id) {
        SysOrg sysOrg = sysOrgRepository.findOnly(Long.parseLong(id));
        if (sysOrg != null) {
            if (sysOrg.getChildren().size() > 0) {
                Set<SysOrg> ids = new HashSet<>();
                getOrgChildIds(sysOrg, ids);
                for (SysOrg org_del : ids) {
                    ModifyBelongUserOrg(org_del.getId());
                }
                sysOrgRepository.deleteInBatch(ids);
            }
            ModifyBelongUserOrg(Long.parseLong(id));
            sysOrgRepository.deleteById(Long.parseLong(id));
            return "success";
        }
        return "failed";
    }

    /**
     * 功能描述: 删除部门后修改用户归属
     *
     * @param id 部门id
     * @author chens
     * @since 2019/12/26 10:38
     */
    private void ModifyBelongUserOrg(Long id) {
        List<SysUser> users = userRepository.findByOrgId(id);
        for (SysUser user_in : users) {
            user_in.setOrg_id(1L);
            userRepository.save(user_in);
        }
    }

    /**
     * 功能描述: 部门新增及修改名称冲突检查
     *
     * @param sysOrg 部门
     * @param isNew  是否新增
     * @return : boolean
     * @author chens
     * @since 2019/12/26 10:38
     */
    private boolean checkOrgConflict(SysOrg sysOrg, boolean isNew) {
        boolean isValid = true;
        Set<SysOrg> ids = new HashSet<>();
        getOrgChildIds(sysOrg, ids);
        for (SysOrg org : ids) {
            if (sysOrg.getPid().equals(org.getId())) {
                return false;
            }
        }
        if (isNew) {
            SysOrg hasSame = sysOrgRepository.findByName(sysOrg.getName());
            if (hasSame != null) {
                isValid = false;
            }
        } else {
            SysOrg origin_org = sysOrgRepository.findOnly(sysOrg.getId());
            if (origin_org != null) {
                if (!origin_org.getName().equals(sysOrg.getName())) {
                    SysOrg hasSame = sysOrgRepository.findByName(sysOrg.getName());
                    if (hasSame != null) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }

    /**
     * 功能描述:获取部门子部门
     *
     * @param sysOrg 父部门
     * @param ids    子部门列表
     * @author chens
     * @since 2019/12/26 10:39
     */
    private void getOrgChildIds(SysOrg sysOrg, Set<SysOrg> ids) {
        for (SysOrg child : sysOrg.getChildren()) {
            ids.add(child);
            if (child.getChildren().size() > 0) {
                getOrgChildIds(child, ids);
            }
        }
    }

    /**
     * 功能描述: 根据状态筛选部门的子部门
     *
     * @param sysOrg  原始部门
     * @param newOrg  拷贝部门
     * @param enabled 状态
     * @author chens
     * @since 2019/12/26 10:39
     */
    private void getOrgChildFilter(SysOrg sysOrg, SysOrg newOrg, boolean enabled) {
        for (SysOrg child : sysOrg.getChildren()) {
            //状态不符合时，直接从拷贝部门删除不符合的部门项
            if (child.isEnabled() != enabled) {
                int index = -1;
                for (SysOrg ch : newOrg.getChildren()) {
                    if (ch.getId().equals(child.getId())) {
                        index = newOrg.getChildren().indexOf(ch);
                    }
                }
                if (index > -1) {
                    newOrg.getChildren().remove(index);
                }
                //状态一致时，检查该子部门的子部门是否符合条件
            } else {
                if (child.getChildren().size() > 0) {
                    for (SysOrg ch : newOrg.getChildren()) {
                        if (ch.getId().equals(child.getId())) {
                            getOrgChildFilter(child, ch, enabled);
                        }
                    }

                }
            }

        }
    }

    /**
     * 功能描述: 修改父部门状态
     *
     * @param current 当前部门
     * @author chens
     * @since 2019/12/26 10:39
     */
    private void ModifyParent(SysOrg current) {
        if (current.getPid() == 1) {
            return;
        }
        List<Boolean> enables = sysOrgRepository.findEnabledByPid(current.getPid());
        SysOrg top_parent = sysOrgRepository.findOnly(current.getPid());
        if (top_parent != null && BaseUtils.any(enables)) {
            top_parent.setEnabled(current.isEnabled());
            SysOrg top_modify = sysOrgRepository.save(top_parent);
            ModifyParent(top_modify);
        }
    }
}
