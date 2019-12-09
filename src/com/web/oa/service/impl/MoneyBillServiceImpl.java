package com.web.oa.service.impl;

import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.BaoxiaoBillExample;
import com.web.oa.service.MoneyBillService;
import com.web.oa.utils.Constants;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

@Service
public class MoneyBillServiceImpl implements MoneyBillService {
    @Autowired
    private BaoxiaoBillMapper baoxiaoBillMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private HistoryService historyService;

    @Override
    public List<BaoxiaoBill> findLeaveBillListByUser(long id) {
        BaoxiaoBillExample baoxiaoBillExample = new BaoxiaoBillExample();
        BaoxiaoBillExample.Criteria criteria = baoxiaoBillExample.createCriteria();
        criteria.andUserIdEqualTo(id);
        return baoxiaoBillMapper.selectByExample(baoxiaoBillExample);
    }

    @Override
    public List<Task> findTaskListByName(String username) {
        System.out.println(this.taskService);
        List<Task> list = this.taskService.createTaskQuery().taskAssignee(username).list();
        return list;
    }

    //发布流程
    @Override
    public void deployProcess(String processName, InputStream in) {
            System.out.println(this.repositoryService);
            ZipInputStream zipInputStream = new ZipInputStream(in);
            this.repositoryService.createDeployment().name(processName).addZipInputStream(zipInputStream).deploy();
    }

    @Override
    public List<Deployment> findDeploymentList() {
        return this.repositoryService.createDeploymentQuery().list();
    }

    @Override
    public List<ProcessDefinition> findDefinitionList() {
        return this.repositoryService.createProcessDefinitionQuery().list();
    }

    @Override
    public void saveBaoxiao(BaoxiaoBill baoxiaoBill) {
        //获取请假单ID
        Long id = baoxiaoBill.getId();
        /*新增保存*/
        if(id==null){
            //1：从Session中获取当前用户对象，将LeaveBill对象中user与Session中获取的用户对象进行关联
            //leaveBill.setUser(SessionContext.get());//建立管理关系
            //2：保存请假单表，添加一条数据
            baoxiaoBillMapper.insert(baoxiaoBill);
        }
        /*更新保存*/
        else{
            //1：执行update的操作，完成更新
            baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
        }
    }

    @Override
    public void saveStartProcess(Long baoxiaoId, String username) {
        //使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
        String key= Constants.BAOXIAO_KEY;
        /*
         * 从Session中获取当前任务的办理人，使用流程变量设置下一个任务的办理人
         * inputUser是流程变量的名称，
         * 获取的办理人是流程变量的值
         */
        Map<String, Object> variables = new HashMap();
        variables.put("inputUser", username);//表示惟一用户

        //格式：baoxiao.id的形式（使用流程变量），存入businessKey
        String objId = key+"."+baoxiaoId;
        variables.put("objId", objId);
        //5：使用流程定义的key，启动流程实例，同时设置流程变量，同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业务
        runtimeService.startProcessInstanceByKey(key,objId,variables);
    }

    //查询当前的报销单信息
    @Override
    public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = this.runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        String bussiness_key = pi.getBusinessKey();
        System.out.println(bussiness_key);
        String id = "";
        if (StringUtils.isNotBlank(bussiness_key)) {
            id = bussiness_key.split("\\.")[1];
        }

        BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));

        return bill;
    }

    //查询所有批注信息
    @Override
    public List<Comment> findCommentByTaskId(String taskId) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<Comment> comments = this.taskService.getProcessInstanceComments(processInstanceId);
        return comments;
    }

    //查询连线情况(之后要控制走向)
    @Override
    public List<String> findOutComeListByTaskId(String taskId) {
        //返回存放连线的名称集合
        List<String> list = new ArrayList<String>();
        //1:使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //2：获取流程定义ID
        String processDefinitionId = task.getProcessDefinitionId();
        //3：查询ProcessDefinitionEntiy对象
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
        //使用任务对象Task获取流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //获取当前活动的id
        String activityId = pi.getActivityId();
        //4：获取当前的活动
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
        //5：获取当前活动完成之后连线的名称
        List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
        if(pvmList!=null && pvmList.size()>0){
            for(PvmTransition pvm:pvmList){
                String name = (String) pvm.getProperty("name");
                if(StringUtils.isNotBlank(name)){
                    list.add(name);
                } else{
                    list.add("默认提交");
                }
            }
        }
        return list;
    }

    @Override
    public void submitTask(Long id, String taskId, String comment, String username, String outcome) {
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        //设置comment表中的userId
        Authentication.setAuthenticatedUserId(username);
        //添加批注
        this.taskService.addComment(taskId, processInstanceId, comment);
        //设置流程变量
        if (outcome!=null&&!outcome.equals("默认提交")){
            Map<String,Object> variables = new HashMap<>();
            variables.put("message", outcome);
            this.taskService.complete(taskId, variables);
        }else {
            this.taskService.complete(taskId);
        }

        //更新任务状态
        ProcessInstance processInstance = this.runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance==null){
            BaoxiaoBill baoxiaoBill = baoxiaoBillMapper.selectByPrimaryKey(id);
            baoxiaoBill.setState(2);
            baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
        }
    }


    //删除流程
    @Override
    public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
        this.repositoryService.deleteDeployment(deploymentId,true);
    }

    //根据id查询报销单信息
    @Override
    public BaoxiaoBill findBaoxiaoBillById(Long id) {
        return baoxiaoBillMapper.selectByPrimaryKey(id);
    }

    //根据业务id查询批注
    @Override
    public List<Comment> findCommentsByBillId(Long id) {
        String businessKey = Constants.BAOXIAO_KEY+"."+id;
        //获取历史流程
        HistoricProcessInstance historicProcessInstance = this.historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
        //根据历史流程id查批注
        return this.taskService.getProcessInstanceComments(historicProcessInstance.getId());
    }

    @Override
    public void deleteBaoXiaoBill(Long leaveBillId) {
        baoxiaoBillMapper.deleteByPrimaryKey(leaveBillId);
    }

    @Override
    public Task findTaskByBussinessKey(String bussiness_key) {
        return this.taskService.createTaskQuery().processInstanceBusinessKey(bussiness_key).singleResult();
    }

    //根据任务id获取流程定义对象
    @Override
    public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
        //使用任务ID，查询任务对象
        Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
        //使用任务ID，查询任务对象
        String processDefinitionId = task.getProcessDefinitionId();
        //查询流程定义的对象
        return this.repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    }

    //获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中
    @Override
    public Map<String, Object> findCoordingByTask(String taskId) {
        //存放坐标
        Map<String, Object> map = new HashMap<String,Object>();
        //使用任务ID，查询任务对象
        Task task = taskService.createTaskQuery()//
                .taskId(taskId)//使用任务ID查询
                .singleResult();
        //获取流程定义的ID
        String processDefinitionId = task.getProcessDefinitionId();
        //获取流程定义的实体对象（对应.bpmn文件中的数据）
        ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
        //流程实例ID
        String processInstanceId = task.getProcessInstanceId();
        //使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        //获取当前活动的ID
        String activityId = pi.getActivityId();
        //获取当前活动对象
        ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
        //获取坐标
        map.put("x", activityImpl.getX());
        map.put("y", activityImpl.getY());
        map.put("width", activityImpl.getWidth());
        map.put("height", activityImpl.getHeight());
        return map;
    }


    //获取流程定义图
    @Override
    public InputStream findImageInputStream(String deploymentId, String imageName) {
        return repositoryService.getResourceAsStream(deploymentId, imageName);
    }


}
