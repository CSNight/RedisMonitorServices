package csnight.redis.monitor.busi.rms;

import com.alibaba.fastjson.JSONObject;
import csnight.redis.monitor.db.jpa.RmsDataRecord;
import csnight.redis.monitor.db.repos.RmsDataRecRepository;
import csnight.redis.monitor.utils.BaseUtils;
import csnight.redis.monitor.utils.RespTemplate;
import csnight.redis.monitor.utils.YamlUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author csnight
 * @description
 * @since 2020/1/20 14:43
 */
@Service
public class RmsDataBackupImpl {
    @Resource
    private RmsDataRecRepository dataRecRepository;

    public List<RmsDataRecord> GetDataRecord() {
        return dataRecRepository.findByCreateUser(BaseUtils.GetUserFromContext());
    }

    public List<RmsDataRecord> GetAllDataRecord() {
        return dataRecRepository.findAllRecord();
    }

    public RmsDataRecord GetDataRecordById(String id) {
        return dataRecRepository.findById(id).orElse(null);
    }

    public String DeleteById(String id) {
        boolean delSuccess = false;
        String recordDir = System.getProperty("user.dir") + "/" + YamlUtils.getStrYmlVal("dumpdir.record-dir") + "/";
        Optional<RmsDataRecord> optDataRecord = dataRecRepository.findById(id);
        if (optDataRecord.isPresent()) {
            RmsDataRecord dataRecord = optDataRecord.get();
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

    public void DownloadBackup(String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<RmsDataRecord> optDataRecord = dataRecRepository.findById(id);
        if (optDataRecord.isEmpty()) {
            response.setContentType("application/json");
            response.setStatus(404);
            response.getWriter().write(JSONObject.toJSONString(
                    new RespTemplate(200, HttpStatus.OK, "File not found", "/backup/download", "DownloadBackup")));
            response.getWriter().flush();
            return;
        }
        RmsDataRecord dataRecord = optDataRecord.get();
        String recordDir = System.getProperty("user.dir") + "/" + YamlUtils.getStrYmlVal("dumpdir.record-dir") + "/";
        String filePath = recordDir + dataRecord.getFilename();
        File downloadFile = new File(filePath);
        if (!downloadFile.exists() || !downloadFile.canRead()) {
            response.setContentType("application/json");
            response.setStatus(400);
            response.getWriter().write(JSONObject.toJSONString(
                    new RespTemplate(200, HttpStatus.OK, "File not found or can not read", "/backup/download", "DownloadBackup")));
            response.getWriter().flush();
            return;
        }
        //保存下载信息
        dataRecord.setDl_count(dataRecord.getDl_count() + 1);
        dataRecord.setLast_down(new Date());
        dataRecRepository.save(dataRecord);
        ServletContext context = request.getServletContext();
        // get MIME type of the file
        String mimeType = context.getMimeType(filePath);
        if (mimeType == null) {
            // set to binary type if MIME mapping not found
            mimeType = "application/octet-stream";
        }
        // set content attributes for the response
        response.setContentType(mimeType);
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        response.setHeader("Accept-Ranges", "bytes");
        long downloadSize = downloadFile.length();
        long fromPos = 0, toPos = 0;
        if (request.getHeader("Range") == null) {
            response.setHeader("Content-Length", downloadSize + "");
        } else {
            // 解析断点续传相关信息
            // 若客户端传来Range，说明之前下载了一部分，设置206状态(SC_PARTIAL_CONTENT)
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            String range = request.getHeader("Range");
            String bytes = range.replaceAll("bytes=", "");
            String[] ary = bytes.split("-");
            fromPos = Long.parseLong(ary[0]);
            if (ary.length == 2) {
                toPos = Long.parseLong(ary[1]);
            }
            int size;
            if (toPos > fromPos) {
                size = (int) (toPos - fromPos);
            } else {
                size = (int) (downloadSize - fromPos);
            }
            response.setHeader("Content-Length", size + "");
            downloadSize = size;
        }
        OutputStream out = response.getOutputStream();
        try (RandomAccessFile in = new RandomAccessFile(downloadFile, "rw")) {

            // 设置下载起始位置
            if (fromPos > 0) {
                in.seek(fromPos);
            }
            // 缓冲区大小
            int bufLen = (int) (downloadSize < 2048 ? downloadSize : 2048);
            byte[] buffer = new byte[bufLen];
            int num;
            int count = 0; // 当前写到客户端的大小
            while ((num = in.read(buffer)) != -1) {
                out.write(buffer, 0, num);
                count += num;
                //处理最后一段，计算不满缓冲区的大小
                if (downloadSize - count < bufLen) {
                    bufLen = (int) (downloadSize - count);
                    if (bufLen == 0) {
                        break;
                    }
                    buffer = new byte[bufLen];
                }
                out.flush();
            }
            response.flushBuffer();
        } catch (IOException e) {
            response.setContentType("application/json");
            response.setStatus(400);
            out.write(JSONObject.toJSONString(
                    new RespTemplate(200, HttpStatus.OK, e.getMessage(), "/backup/download", "DownloadBackup")).getBytes());
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}
