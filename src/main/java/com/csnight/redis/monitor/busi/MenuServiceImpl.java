package com.csnight.redis.monitor.busi;

import com.csnight.redis.monitor.aop.QueryAnnotationProcess;
import com.csnight.redis.monitor.busi.exp.MenuQueryExp;
import com.csnight.redis.monitor.db.jpa.SysIcons;
import com.csnight.redis.monitor.db.jpa.SysMenu;
import com.csnight.redis.monitor.db.repos.SysIconRepository;
import com.csnight.redis.monitor.db.repos.SysMenuRepository;
import com.csnight.redis.monitor.rest.dto.MenuDto;
import com.csnight.redis.monitor.utils.BaseUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MenuServiceImpl {
    @Resource
    private SysMenuRepository sysMenuRepository;
    @Resource
    private SysIconRepository sysIconRepository;

    public List<SysIcons> GetIconList() {
        return sysIconRepository.findAll();
    }

    public List<SysMenu> GetMenuList() {
        List<SysMenu> menuList = sysMenuRepository.findAll(Sort.by("sort"));
        for (SysMenu sysMenu : menuList) {
            sysMenu.setChildren(null);
        }
        return menuList;
    }

    public List<SysMenu> QueryBy(MenuQueryExp exp) {
        List<SysMenu> menuList = sysMenuRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryAnnotationProcess.getPredicate(root, exp, criteriaBuilder));
        for (SysMenu sysMenu : menuList) {
            sysMenu.setChildren(null);
        }
        return menuList;
    }

    public List<SysMenu> GetMenuTree() {
        return sysMenuRepository.findByPidOrderBySortAsc(0L);
    }

    public List<SysMenu> GetMenuByPid(String pid) {
        return sysMenuRepository.findByPidOrderBySortAsc(Long.parseLong(pid));
    }

    public SysMenu ModifyMenu(MenuDto menuDto) {
        Optional<SysMenu> sysMenu = sysMenuRepository.findById(menuDto.getId());
        if (sysMenu.isPresent()) {
            SysMenu old_menu = sysMenu.get();
            boolean hidden = old_menu.isHidden();
            old_menu.setComponent(menuDto.getComponent());
            old_menu.setComponent_name(menuDto.getComponent_name());
            old_menu.setIcon(menuDto.getIcon());
            old_menu.setHidden(menuDto.isHidden());
            old_menu.setName(menuDto.getName());
            old_menu.setPath(menuDto.getPath());
            old_menu.setSort(menuDto.getSort());
            old_menu.setIframe(menuDto.isIframe());
            old_menu.setPid(menuDto.getPid());
            if (checkMenuConflict(old_menu, false)) {
                SysMenu res = sysMenuRepository.save(old_menu);
                if (old_menu.isHidden() != hidden) {
                    Set<SysMenu> ids = new HashSet<>();
                    getMenuChildIds(old_menu, ids);
                    for (SysMenu child : ids) {
                        child.setHidden(res.isHidden());
                        sysMenuRepository.save(child);
                    }
                }
                ModifyParent(res);
                return res;
            }
        }
        return null;
    }

    public SysMenu NewMenu(MenuDto menuDto) {
        SysMenu new_menu = new SysMenu();
        if (menuDto.getPid().equals(0) && !menuDto.isIframe()) {
            new_menu.setPath("/" + menuDto.getComponent_name());
        } else if (menuDto.getPid() > 0 && !menuDto.isIframe()) {
            Optional<SysMenu> parent = sysMenuRepository.findById(menuDto.getPid());
            parent.ifPresent(sysMenu -> new_menu.setPath(sysMenu.getPath() + "/" + menuDto.getComponent_name()));
        } else {
            new_menu.setPath(menuDto.getPath());
        }
        new_menu.setComponent(menuDto.getComponent());
        new_menu.setComponent_name(menuDto.getComponent_name());
        new_menu.setIcon(menuDto.getIcon());
        new_menu.setHidden(menuDto.isHidden());
        new_menu.setName(menuDto.getName());
        new_menu.setSort(menuDto.getSort());
        new_menu.setIframe(menuDto.isIframe());
        new_menu.setPid(menuDto.getPid());
        new_menu.setCreate_time(new Date());
        if (checkMenuConflict(new_menu, true)) {
            return sysMenuRepository.save(new_menu);
        }
        return null;
    }

    public String DeleteMenuById(String id) {
        Optional<SysMenu> sysMenuOpt = sysMenuRepository.findById(Long.parseLong(id));
        if (sysMenuOpt.isPresent()) {
            SysMenu sysMenu = sysMenuOpt.get();
            if (sysMenu.getChildren().size() > 0) {
                Set<SysMenu> ids = new HashSet<>();
                getMenuChildIds(sysMenu, ids);
                sysMenuRepository.deleteInBatch(ids);
            }
            sysMenuRepository.deleteById(Long.parseLong(id));
            return "success";
        }
        return "failed";
    }

    private void getMenuChildIds(SysMenu sysMenu, Set<SysMenu> ids) {
        for (SysMenu child : sysMenu.getChildren()) {
            ids.add(child);
            if (child.getChildren().size() > 0) {
                getMenuChildIds(child, ids);
            }
        }
    }

    private void ModifyParent(SysMenu current) {
        if (current.getPid() == 0) {
            return;
        }
        List<Boolean> enables = sysMenuRepository.findHiddenByPid(current.getPid());
        Optional<SysMenu> top_parent_option = sysMenuRepository.findById(current.getPid());
        if (top_parent_option.isPresent() && BaseUtils.any(enables)) {
            SysMenu top_parent = top_parent_option.get();
            top_parent.setHidden(current.isHidden());
            SysMenu top_modify = sysMenuRepository.save(top_parent);
            ModifyParent(top_modify);
        }
    }


    private boolean checkMenuConflict(SysMenu sysMenu, boolean isNew) {
        boolean isValid = true;
        Set<SysMenu> ids = new HashSet<>();
        getMenuChildIds(sysMenu, ids);
        for (SysMenu menu : ids) {
            if (sysMenu.getPid().equals(menu.getId())) {
                return false;
            }
        }
        if (isNew) {
            SysMenu hasSame = sysMenuRepository.findByName(sysMenu.getName());
            if (hasSame != null) {
                isValid = false;
            }
            SysMenu hasSameCom = sysMenuRepository.findByComponent_name(sysMenu.getComponent_name());
            if (hasSameCom != null) {
                isValid = false;
            }
        } else {
            Optional<SysMenu> original = sysMenuRepository.findById(sysMenu.getId());
            if (original.isPresent()) {
                SysMenu origin_menu = original.get();
                if (!origin_menu.getName().equals(sysMenu.getName())) {
                    SysMenu hasSame = sysMenuRepository.findByName(sysMenu.getName());
                    if (hasSame != null) {
                        isValid = false;
                    }
                }
                if (!origin_menu.getComponent_name().equals(sysMenu.getComponent_name())) {
                    SysMenu hasSameCom = sysMenuRepository.findByComponent_name(sysMenu.getComponent_name());
                    if (hasSameCom != null) {
                        isValid = false;
                    }
                }
            }
        }
        return isValid;
    }
}
