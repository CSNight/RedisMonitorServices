package com.redis.monitor.auth.config;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.List;

public class JdbcTokenRepositoryExt extends JdbcTokenRepositoryImpl {

    public List<PersistentRememberMeToken> getTokenForName(String username) {
        try {
            String tokensByUserSql = "select username,series,token,last_used from persistent_logins where username = ? order by last_used desc";
            return this.getJdbcTemplate() != null ? this.getJdbcTemplate().query(tokensByUserSql, (rs, rowNum) -> new PersistentRememberMeToken(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4)), username) : null;
        } catch (EmptyResultDataAccessException var3) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Querying token for series '" + username + "' returned no results.", var3);
            }
        } catch (IncorrectResultSizeDataAccessException var4) {
            this.logger.error("Querying token for series '" + username + "' returned more than one value. Series should be unique");
        } catch (DataAccessException var5) {
            this.logger.error("Failed to load token for series " + username, var5);
        }
        return null;
    }

    public void removeUserOldToken(String username, String token) {
        String removeUserTokenSql = "delete from persistent_logins where username = ? and token = ?";
        assert this.getJdbcTemplate() != null;
        this.getJdbcTemplate().update(removeUserTokenSql, username, token);
    }
}
