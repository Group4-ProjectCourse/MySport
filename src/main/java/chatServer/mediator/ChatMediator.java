package chatServer.mediator;




import chatServer.observable.listeners.RemoteListener;
import chatServer.observable.subjects.RemoteSubject;
import chatServer.repository.ChatDAO;
import chatServer.clientModels.Conversation;
import chatServer.clientModels.Message;
import chatServer.clientModels.User;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMediator implements RemoteSubject {

    private Map<String, RemoteListener> listenerUserMap;
    private ChatDAO model;

    public ChatMediator(ChatDAO model) {
        listenerUserMap = new HashMap<String, RemoteListener>();
        this.model = model;
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            UnicastRemoteObject.exportObject(this, 0);
            Naming.rebind("ChatServer", this);
            System.out.println("ChatMediator has been bound to port 1099");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChatDAO dao = new ChatDAO();
        ChatMediator chatMediator = new ChatMediator(dao);
    }
/*
sendMessage
    insert message into DB and get its ID
    insert new conversation_message into DB
    return to Chatmediator
        Look for Users who are part of this conversation from DB
        For each User found, search the active listeners Map for Listeners mapped by mobileNumber
        call receiveMessage method on them
  addListener
    receive mobileNumber and Remotelistener
    put key and value into Map
  newConversation
    insert message into DB and get its ID
    create new conversation and get its ID
    insert new conversation_message into DB
    Look for User whose mobileNumber is fromID
    insert new user_conversation into DB
    Look for User whose mobileNumber is toID
    insert new user_conversation into DB
    return to Chatmediator
        Look for RemoteListener mapped by mobileNumber in fromID
        call receiveMessage method on it
        Look for RemoteListener mapped by mobileNumber in toID
        call receiveMessage method on it
  loadConversations
    Search for User whose mobileNumber is userID supplied
    Search user_conversation where userid = User.id
    For each result, load Conversation where id is same
    return conversations to ChatMediator
        Look for RemoteListener mapped by mobileNumber in userID
        call receiveConversations
  loadTeamMembersContacts
    load all users in the DB
    return conversations to ChatMediator
        Look for RemoteListener mapped by mobileNumber in userID
        call receiveUsers


 */
    @Override
    public boolean addListener(String userID, RemoteListener var) throws RemoteException {
        System.out.println("Add listener called on userID: " + userID);
        listenerUserMap.put(userID, var);
        return false;
    }

    @Override
    public boolean removeListener(String userID, RemoteListener var) throws RemoteException {
        listenerUserMap.remove(userID);
        return false;
    }

    public void sendMessage(Message msg, int conversationID, String fromID) throws RemoteException{
        int messageID = model.insertMessage(msg);
        model.insertConversationMessage(messageID, conversationID);
        List<User> usersInConversation = model.getUsersInConversation(conversationID);
        for(User user : usersInConversation){
            RemoteListener rm = listenerUserMap.get(user.getMobile());
            if(rm != null){
                rm.receiveMessage(msg, conversationID);
            }
        }
    }

    public void newConversation(Message msg, String fromID, String toID) throws RemoteException{
        int messageID = model.insertMessage(msg);
        int newConversationID = model.createNewConversation();
        model.insertConversationMessage(messageID, newConversationID);
        User user1 = model.getUser(fromID);
        if(user1 != null){
            model.insertUserConversation(user1.getId(), newConversationID);
            RemoteListener rm = listenerUserMap.get(user1.getMobile());
            if(rm != null){
                rm.receiveMessage(msg, newConversationID);
            }
        }
        User user2 = model.getUser(toID);
        if(user2 != null){
            model.insertUserConversation(user2.getId(), newConversationID);
            RemoteListener rm = listenerUserMap.get(user2.getMobile());
            if(rm != null){
                rm.receiveMessage(msg, newConversationID);
            }
        }
    }

    public List<Conversation> loadConversations(String mobileNumber) throws RemoteException{
        User user = model.getUser(mobileNumber);
        if(user != null){
            List<Conversation> conversations = model.getConversationsForUser(user.getId());
            return conversations;
            /*
            RemoteListener rm = listenerUserMap.get(user.getMobile());
            if(rm != null){
                rm.receiveConversations(conversations);
            }

             */
        }
        return null;
    }

    public List<User> loadTeamMembersContacts(String mobileNumber) throws RemoteException{
        System.out.println("loadTeamMembersContacts called for " + mobileNumber);
        List<User> users = model.getAllUsersNot(mobileNumber);
        return users;
        //RemoteListener rm = listenerUserMap.get(mobileNumber);
        /*
        if(rm != null){
            rm.receiveUsers(users);
        }
         */
    }
}
