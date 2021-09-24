package model;

import events.MessageDeleteListener;
import events.ReactionEvent.MessageReactionListener;
import events.StudyTimeEvent.StudyTimeLogger;

import events.StudyTimeEvent.StudyTimeRecord;
import org.bson.Document;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.util.logging.FallbackLoggerConfiguration;
import persistence.DBReader;
import persistence.DBWriter;

import java.util.function.Consumer;


public class Bot {
    public static DiscordApi API;

    public static void main(String[] args) {
        String COLLECTION_NAME = "study_times";
        DBReader reader = new DBReader(COLLECTION_NAME);
        DBWriter writer = new DBWriter(COLLECTION_NAME);
        reader.loadAllDocuments().forEach((Consumer<? super Document>) document -> {
            document.put(StudyTimeRecord.GLOBAL_STUDY_TIME_KEY,document.getLong("study_time"));
            document.put(StudyTimeRecord.WEEKLY_STUDY_TIME_KEY,0);
            document.remove("study_time");
            writer.saveDocument(document);
        });
        FallbackLoggerConfiguration.setDebug(true);
        FallbackLoggerConfiguration.setTrace(true);
        API = new DiscordApiBuilder()
        .setToken(System.getenv("discord_token"))
        .setAllIntentsExcept(Intent.GUILD_PRESENCES, Intent.GUILD_WEBHOOKS)
        .login().join();
        API.addMessageCreateListener(new MessageListener());
        API.addListener(new StudyTimeLogger());
        API.addMessageDeleteListener(new MessageDeleteListener());
        API.addListener(new MessageReactionListener());
        new DailyExecutor();
    }
}

