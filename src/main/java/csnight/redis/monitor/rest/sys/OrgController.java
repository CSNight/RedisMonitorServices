package csnight.redis.monitor.rest.sys;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.aop.LogAsync;
import csnight.redis.monitor.busi.sys.OrgServiceImpl;
import csnight.redis.monitor.busi.sys.exp.OrgQueryExp;
import csnight.redis.monitor.db.jpa.SysOrg;
import csnight.redis.monitor.exception.ConflictsException;
import csnight.redis.monitor.utils.RespTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "org")
@Api(tags = "组织机构API")
public class OrgController {

    private OrgServiceImpl userService;

    public OrgController(OrgServiceImpl userService) {
        this.userService = userService;
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_QUERY")
    @PreAuthorize("hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "查询组织机构目录树")
    @RequestMapping(value = "/get_org_tree", method = RequestMethod.GET)
    public RespTemplate GetOrgTree() {
        return new RespTemplate(HttpStatus.OK, userService.GetOrgTree());
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_QUERY")
    @PreAuthorize("hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "查询组织机构目录列表")
    @RequestMapping(value = "/get_org_list", method = RequestMethod.GET)
    public RespTemplate GetOrgList() {
        return new RespTemplate(HttpStatus.OK, userService.GetOrgList());
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_QUERY")
    @PreAuthorize("hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "搜索组织机构")
    @RequestMapping(value = "/query_org", method = RequestMethod.GET)
    public RespTemplate OrgQuery(OrgQueryExp exp) {
        return new RespTemplate(HttpStatus.OK, userService.QueryBy(exp));
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_QUERY")
    @PreAuthorize("hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "根据ID及状态查询组织机构")
    @RequestMapping(value = "/get_org_by", method = RequestMethod.GET)
    public RespTemplate OrgQueryBy(String id, boolean enabled) {
        return new RespTemplate(HttpStatus.OK, userService.GetOrgByIdAndEnabled(id, enabled));
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_QUERY")
    @PreAuthorize("hasAuthority('ORG_QUERY')")
    @ApiOperation(value = "通过父节点ID获取组织机构目录")
    @RequestMapping(value = "/{pid}/get_org", method = RequestMethod.GET)
    public RespTemplate GetOrgByPid(@PathVariable String pid) {
        return new RespTemplate(HttpStatus.OK, userService.GetOrgByPid(pid));
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_UPDATE")
    @PreAuthorize("hasAuthority('ORG_UPDATE')")
    @ApiOperation(value = "修改组织机构")
    @RequestMapping(value = "/modify_org", method = RequestMethod.PUT)
    public RespTemplate ModifyOrgIns(@RequestParam("org_ent") String org_ent) throws ConflictsException {
        if (org_ent != null && !org_ent.equals("")) {
            return new RespTemplate(HttpStatus.OK, userService.ModifyOrg(JSONObject.parseObject(org_ent)));
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_ADD")
    @PreAuthorize("hasAuthority('ORG_ADD')")
    @ApiOperation(value = "添加组织机构")
    @ApiImplicitParam(paramType = "query", name = "org_ent", value = "新组织", required = true, dataType = "String")
    @RequestMapping(value = "/new_org", method = RequestMethod.POST)
    public RespTemplate NewOrgIns(@RequestParam("org_ent") String org_ent) throws ConflictsException {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String name = userDetails.getUsername();
        if (org_ent != null && !org_ent.equals("")) {
            SysOrg new_one = userService.NewOrg(JSONObject.parseObject(org_ent), name);
            return new RespTemplate(new_one != null ? HttpStatus.OK : HttpStatus.CONFLICT, new_one != null ? new_one : "Exists organizations contains conflicts!");
        }
        return new RespTemplate(HttpStatus.BAD_REQUEST, "");
    }

    @LogAsync(module = "DEPARTS", auth = "ORG_DEL")
    @PreAuthorize("hasAuthority('ORG_DEL')")
    @ApiOperation(value = "通过ID删除组织机构")
    @RequestMapping(value = "/delete_org/{id}", method = RequestMethod.DELETE)
    public RespTemplate DeleteOrgById(@PathVariable String id) {
        return new RespTemplate(HttpStatus.OK, userService.DeleteOrgById(id));
    }
}
