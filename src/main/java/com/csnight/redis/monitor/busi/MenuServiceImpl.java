package com.csnight.redis.monitor.busi;

import com.csnight.redis.monitor.db.jpa.SysMenu;
import com.csnight.redis.monitor.db.repos.SysMenuRepository;
import com.csnight.redis.monitor.rest.dto.MenuDto;
import com.csnight.redis.monitor.utils.BaseUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuServiceImpl {
    private SysMenuRepository sysMenuRepository;

    public MenuServiceImpl(SysMenuRepository sysMenuRepository) {
        this.sysMenuRepository = sysMenuRepository;
    }

    public List<SysMenu> GetMenuList() {
        List<SysMenu> menuList = sysMenuRepository.findAll();
        for (SysMenu sysMenu : menuList) {
            sysMenu.setChildren(null);
        }
        return menuList;
    }

    public List<SysMenu> GetMenuTree() {
        return sysMenuRepository.findByPid(0L);
    }

    public List<SysMenu> GetMenuByPid(String pid) {
        return sysMenuRepository.findByPid(Long.parseLong(pid));
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
        }
        return null;
    }

    public SysMenu NewMenu(MenuDto menuDto) {
        SysMenu new_menu = new SysMenu();
        new_menu.setComponent(menuDto.getComponent());
        new_menu.setComponent_name(menuDto.getComponent_name());
        new_menu.setIcon(menuDto.getIcon());
        new_menu.setHidden(menuDto.isHidden());
        new_menu.setName(menuDto.getName());
        new_menu.setPath(menuDto.getPath());
        new_menu.setSort(menuDto.getSort());
        new_menu.setIframe(menuDto.isIframe());
        new_menu.setPid(menuDto.getPid());
        new_menu.setCreate_time(new Date());
        return sysMenuRepository.save(new_menu);
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
        if (current.getPid() == 1) {
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

}
