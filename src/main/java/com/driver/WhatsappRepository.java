package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below-mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashMap<String,User> userMap;
    private HashMap<String,Group> groupHashMap;
    private HashMap<Integer,Message> messageHashMap;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMap = new HashMap<>();
        this.groupHashMap=new HashMap<>();
        this.messageHashMap=new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        if(userMap.containsKey(mobile)){
            throw new Exception("User already exists");
        }
        User user=new User(name,mobile);
        userMap.put(mobile,user);
        return "SUCCESS";
    }

    public Group createGroup(List<User> users) throws Exception {
        // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name
        // of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group count". For example, the name of first group
        // would be "Group 1", second would be "Group 2" and so on. Note that a personal chat is not considered a
        // group and the count is not updated for personal chats. If group is successfully created, return group.

        // For example: Consider
        // userList1 = {Alex, Bob, Charlie},
        // userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
        // If createGroup is called for these userLists in the same order,
        // their group names would be "Group 1", "Evan", and "Group 2" respectively.

        // SOLUTION
        // first check if all user is available in the userMap or not
        // check how many user are available in the list
        // if 2 -> then don't update the groupCount
        for(User user: users){
            if(!userMap.containsKey(user.getMobile())){
                throw new Exception(user.getName()+" doesn't exist");
            }
        }
        // ideally we should check if the personal chat of group is already exist or not
        if(users.size()==2){
            User user1=userMap.get(users.get(0).getMobile());
            User user2 =userMap.get(users.get(1).getMobile());
            if(groupHashMap.containsKey(user2.getName()))
                throw new Exception("Personal Chat is already available");
            Group group=new Group(user2.getName(),2);
            groupHashMap.put(user2.getName(),group);
            users=new ArrayList<>();
            users.add(user1);
            users.add(user2);
            groupUserMap.put(group,users);
            return group;
        }
        // if all user already exist and its not personal chat then  we create the group with given details
//        customGroupCount++;
        String groupName="Group "+customGroupCount++;
        User admin=userMap.get(users.get(0).getMobile());
        List<User> groupMembers=new ArrayList<>();
        for(User user:users){
            groupMembers.add(userMap.get(user.getMobile()));
        }
        Group group=new Group(groupName,groupMembers.size());
        groupHashMap.put(groupName,group);
        groupUserMap.put(group,groupMembers);
        adminMap.put(group,admin);
        return group;
    }

    public int createMessage(String content) {
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        Message message=new Message(messageId++,content,new Date());
        messageHashMap.put(messageId,message);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        if(!groupHashMap.containsKey(group.getName())){
            throw new Exception("Group does not exit");
        }
        if(!userMap.containsKey(sender.getMobile())){
            throw new Exception("Sender does not exist");
        }
        sender=userMap.get(sender.getMobile());
        group=groupHashMap.get(group.getName());
        List<User> users=groupUserMap.get(group);
        for(User user: users){
            if(!user.equals(sender)){
                throw new Exception("You are not allowed to send message");
            }
        }
        if(!groupMessageMap.containsKey(group)){
            groupMessageMap.put(group,new ArrayList<>());
        }

        // to confirm that is message id exist or not
            if(!messageHashMap.containsKey(message.getId())){
                throw new Exception("Message doesn't exit");
            }
            Message newMessage=messageHashMap.get(message.getId());
            newMessage.setContent(message.getContent());
            newMessage.setTimestamp(message.getTimestamp());
        groupMessageMap.get(group).add(newMessage);
        senderMap.put(newMessage,sender);
        return groupMessageMap.get(group).size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".
        // Note that at one time there is only one admin and the admin rights are transferred from approver to user.

        if(!groupHashMap.containsKey(group.getName())){
            throw new Exception("Group does not exist");
        }
        group=groupHashMap.get(group.getName());
        if(!userMap.containsKey(user.getMobile())){
            throw new Exception("User doesn't exist");
        }
        user=userMap.get(user.getMobile());
        if(user.equals(adminMap.get(group))){
            throw new Exception("Approver does not have rights");
        }
        List<User> users=groupUserMap.get(group);
        boolean isPartOfGroup=false;
        for(User user1: users){
            if(user1.equals(user)){
                isPartOfGroup=!isPartOfGroup;
            }
        }
        if(!isPartOfGroup){
            throw new Exception("User is not a participant");
        }
        adminMap.put(group,user);
        return "SUCCESS";

    }

    public int removeUser(User user) throws Exception {
        //This is a bonus problem and does not contain any marks
        //A user belongs to exactly one group
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group, and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases,
        // and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group +
        // the updated number of messages in group + the updated number of overall messages)

        if(!userMap.containsKey(user.getMobile())){
            throw new Exception("User not found");
        }
        user=userMap.get(user.getMobile());
        boolean isPartOfGroup=false;
        Group group=null;
        for(Group group1: groupUserMap.keySet()){
            for(User user1: groupUserMap.get(group1)){
                if(user.equals(user1)){
                    group=group1;
                    isPartOfGroup=!isPartOfGroup;
                }
            }
        }
        if(!isPartOfGroup){
            throw new Exception("User not found");
        }
        if(adminMap.get(group).equals(user)){
            throw new Exception("Cannot remove admin");
        }
        groupUserMap.get(group).remove(user);

        // now for removing message from the user
        List<Message> messages=new ArrayList<>();
        for(Message message1: senderMap.keySet()){
            if(user.equals(senderMap.get(message1))){
                messages.add(message1);
            }
        }
        // removing all messages
        for(Message message: messages){
            senderMap.remove(message);
            messageHashMap.remove(message.getId());
        }

        return groupUserMap.get(group).size();

    }

    public String findMessage(Date start, Date end, int k)throws Exception {
        //This is a bonus problem and does not contain any marks
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        List<Message> messages=new ArrayList<>();
        for(Message message: messageHashMap.values()){
            if(message.getTimestamp().compareTo(start)>0 && message.getTimestamp().compareTo(end)<0){
                messages.add(message);
            }
        }
        if(messages.size()<k)
            throw new Exception("K is greater than the number of messages");
        Comparator<Message> com=new Comparator<>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        };
        messages.sort(com);
        return messages.get(k-1).getContent();
    }
}
