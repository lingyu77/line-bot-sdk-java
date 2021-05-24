/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.example.bot.spring;

import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MemberJoinedEvent;
import com.linecorp.bot.model.event.MemberLeftEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.UnknownEvent;
import com.linecorp.bot.model.event.UnsendEvent;
import com.linecorp.bot.model.event.VideoPlayCompleteEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.FileMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@Slf4j
@LineMessageHandler
public class KitchenSinkController {
    @Autowired
    private LineMessagingClient lineMessagingClient;

    @Autowired
    private LineBlobClient lineBlobClient;

    private static List<String> sentence = new ArrayList<>(
            Arrays.asList(" 很棒喔! 第一天啟動了，要繼續維持下去!", " 好的開始，是成功的一半! 加油!", " Well begun is half done. GO! GO!"));

    private static Map<String, String> nickname = new HashMap<String, String>() {{
        put("ichih", "學長");
        put("葉靜芬", "葉講師");
        put("溶", "書溶");
        put("新芳", "祁講師");
        put("sophia 真 楊講師", "楊講師");
        put("鄭鴻儒", "鴻儒");
        put("Joyce 螢軒", "螢軒");
        put("吳佳鴻", "佳鴻");
        put("Shih When 王施雯", "施雯");
        put("楊佩儒", "佩儒");
        put("陳勁源", "勁源");
    }};

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
        TextMessageContent message = event.getMessage();
        this.handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        log.info("handleStickerMessageEvent: {} ", event);
        //handleSticker(event.getReplyToken(), event.getMessage());
    }

    @EventMapping
    public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
        log.info("handleLocationMessageEvent: {} ", event);
//        LocationMessageContent locationMessage = event.getMessage();
//        reply(event.getReplyToken(), new LocationMessage(
//                locationMessage.getTitle(),
//                locationMessage.getAddress(),
//                locationMessage.getLatitude(),
//                locationMessage.getLongitude()
//        ));
    }

    @EventMapping
    public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
        log.info("handleImageMessageEvent: {} ", event);
        // You need to install ImageMagick
//        handleHeavyContent(
//                event.getReplyToken(),
//                event.getMessage().getId(),
//                responseBody -> {
//                    final ContentProvider provider = event.getMessage().getContentProvider();
//                    final DownloadedContent jpg;
//                    final DownloadedContent previewImg;
//                    if (provider.isExternal()) {
//                        jpg = new DownloadedContent(null, provider.getOriginalContentUrl());
//                        previewImg = new DownloadedContent(null, provider.getPreviewImageUrl());
//                    } else {
//                        jpg = saveContent("jpg", responseBody);
//                        previewImg = createTempFile("jpg");
//                        system(
//                                "convert",
//                                "-resize", "240x",
//                                jpg.path.toString(),
//                                previewImg.path.toString());
//                    }
//                    reply(event.getReplyToken(),
//                          new ImageMessage(jpg.getUri(), previewImg.getUri()));
//                });
    }

    @EventMapping
    public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
        log.info("handleAudioMessageEvent: {} ", event);
//        handleHeavyContent(
//                event.getReplyToken(),
//                event.getMessage().getId(),
//                responseBody -> {
//                    final ContentProvider provider = event.getMessage().getContentProvider();
//                    final DownloadedContent mp4;
//                    if (provider.isExternal()) {
//                        mp4 = new DownloadedContent(null, provider.getOriginalContentUrl());
//                    } else {
//                        mp4 = saveContent("mp4", responseBody);
//                    }
//                    reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
//                });
    }

    @EventMapping
    public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
        log.info("Got video message: duration={}ms", event.getMessage().getDuration());

//        // You need to install ffmpeg and ImageMagick.
//        handleHeavyContent(
//                event.getReplyToken(),
//                event.getMessage().getId(),
//                responseBody -> {
//                    final ContentProvider provider = event.getMessage().getContentProvider();
//                    final DownloadedContent mp4;
//                    final DownloadedContent previewImg;
//                    if (provider.isExternal()) {
//                        mp4 = new DownloadedContent(null, provider.getOriginalContentUrl());
//                        previewImg = new DownloadedContent(null, provider.getPreviewImageUrl());
//                    } else {
//                        mp4 = saveContent("mp4", responseBody);
//                        previewImg = createTempFile("jpg");
//                        system("convert",
//                               mp4.path + "[0]",
//                               previewImg.path.toString());
//                    }
//                    String trackingId = UUID.randomUUID().toString();
//                    log.info("Sending video message with trackingId={}", trackingId);
//                    reply(event.getReplyToken(),
//                          VideoMessage.builder()
//                                      .originalContentUrl(mp4.getUri())
//                                      .previewImageUrl(previewImg.uri)
//                                      .trackingId(trackingId)
//                                      .build());
//                });
    }

    @EventMapping
    public void handleVideoPlayCompleteEvent(VideoPlayCompleteEvent event) throws IOException {
        log.info("Got video play complete: tracking id={}", event.getVideoPlayComplete().getTrackingId());
//        this.replyText(event.getReplyToken(),
//                       "You played " + event.getVideoPlayComplete().getTrackingId());
    }

    @EventMapping
    public void handleFileMessageEvent(MessageEvent<FileMessageContent> event) {
        log.info("handleFileMessageEvent: {} ", event);
//        this.reply(event.getReplyToken(),
//                   new TextMessage(String.format("Received '%s'(%d bytes)",
//                                                 event.getMessage().getFileName(),
//                                                 event.getMessage().getFileSize())));
    }

    @EventMapping
    public void handleUnfollowEvent(UnfollowEvent event) {
        log.info("unfollowed this bot: {}", event);
    }

    @EventMapping
    public void handleUnknownEvent(UnknownEvent event) {
        log.info("Got an unknown event!!!!! : {}", event);
    }

    @EventMapping
    public void handleFollowEvent(FollowEvent event) {
        log.info("handleFollowEvent: {} ", event);
//        String replyToken = event.getReplyToken();
//        this.replyText(replyToken, "Got followed event");
    }

    @EventMapping
    public void handleJoinEvent(JoinEvent event) {
//        String replyToken = event.getReplyToken();
      log.info("Joined {}", event.getSource());
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken,
                       "Got postback data " + event.getPostbackContent().getData() + ", param " + event
                               .getPostbackContent().getParams().toString());
    }

    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) {
        String replyToken = event.getReplyToken();
        this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
    }

    @EventMapping
    public void handleMemberJoined(final MemberJoinedEvent event) {
        log.info("handleMemberJoined {}", event);
        final String replyToken = event.getReplyToken();

        event.getJoined().getMembers().stream().map(Source::getUserId).forEach(
            userId -> {
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text( profile.getDisplayName() + " 歡迎您加入防疫團! 相關公告請至記事本觀看喔")
                                                    .build());
                                });
                    }
                }
            }
        );
    }

    @EventMapping
    public void handleMemberLeft(MemberLeftEvent event) {
        log.info("Got memberLeft message: {}", event.getLeft().getMembers()
                                                    .stream().map(Source::getUserId)
                                                    .collect(Collectors.joining(",")));
    }

    @EventMapping
    public void handleMemberLeft(UnsendEvent event) {
        log.info("Got unsend event: {}", event);
    }

    @EventMapping
    public void handleOtherEvent(Event event) {
        log.info("Received message(Ignored): {}", event);
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        reply(replyToken, messages, false);
    }

    private void reply(@NonNull String replyToken,
                       @NonNull List<Message> messages,
                       boolean notificationDisabled) {
        try {
            BotApiResponse apiResponse = lineMessagingClient
                    .replyMessage(new ReplyMessage(replyToken, messages, notificationDisabled))
                    .get();
            log.info("Sent messages: {}", apiResponse);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleHeavyContent(String replyToken, String messageId,
                                    Consumer<MessageContentResponse> messageConsumer) {
        final MessageContentResponse response;
        try {
            response = lineBlobClient.getMessageContent(messageId)
                                     .get();
        } catch (InterruptedException | ExecutionException e) {
            reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
            throw new RuntimeException(e);
        }
        messageConsumer.accept(response);
    }

    private void handleSticker(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(
                content.getPackageId(), content.getStickerId())
        );
    }

    private void handleTextContent(final String replyToken, final Event event, final TextMessageContent content) throws Exception {
        final String text = content.getText();
        int item = 0;
        if (text.contains("運動") || text.contains("快走") || text.contains("跑步") || text.contains("跳繩") || text.contains("核心") || text.contains("瑜珈")) {
            item = 1;
        } else if (text.equals("byetaohelper")) {
            item = 999;
        } else if (text.contains("testEcho")) {
            item = 2;
        } else if (text.contains("D1")) {
            item = 3;
        } else if (text.contains("小道親")) {
            if (text.contains("早安~")) {
                item = 4;
            } else if (text.contains("晚安囉")) {
                item = 5;
            } else if (text.contains("厲害") || text.contains("好棒")  || text.contains("讚")) {
                item = 6;
            }
        } else if (text.contains("D30") || text.contains("D 30") || text.contains("d30") && text.contains("分鐘")) {
            item = 30;
        }

        log.info("Got text message from replyToken:{}: text:{} emojis:{}", replyToken, text, content.getEmojis());
        switch (item) {
            case 30:
            {
                final String userId = event.getSource().getUserId();
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text(this.getNickname(profile.getDisplayName()) + " 棒棒! 堅持30天，完成目標了")
                                                    .build());
                                });
                    }
                }
                break;
            }
            case 4 : {
                final String userId = event.getSource().getUserId();
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text(this.getNickname(profile.getDisplayName()) + " 早安 ")
                                                    .build());
                                });
                    }
                }
                break;
            }
            case 5:{
                final String userId = event.getSource().getUserId();
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text(this.getNickname(profile.getDisplayName()) + " 晚安 ")
                                                    .build());
                                });
                    }
                }
                break;
            }
            case 6:{
                final String userId = event.getSource().getUserId();
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text(this.getNickname(profile.getDisplayName()) + " 謝謝您的稱讚! 小道親會隨時在防疫團為大家打氣的 (握拳) ")
                                                    .build());
                                });
                    }
                }
                break;
            }
            case 1: {
                String packageId = "";
                String stickerId = "";
                Random rand = new Random();
                int upperbound = 12;
                //generate random values from 0-3
                int random = rand.nextInt(upperbound);
                switch (random) {
                    case 0:
                        packageId = "6362";
                        stickerId = "11087933";
                        break;
                    case 1:
                        packageId = "6362";
                        stickerId = "11087942";
                        break;
                    case 2:
                        packageId = "446";
                        stickerId = "1989";
                        break;
                    case 3:
                        packageId = "446";
                        stickerId = "1993";
                        break;
                    case 4:
                        packageId = "8522";
                        stickerId = "16581276";
                        break;
                    case 5:
                        packageId = "8525";
                        stickerId = "16581302";
                        break;
                    case 6:
                        packageId = "11537";
                        stickerId = "52002735";
                        break;
                    case 7:
                        packageId = "11537";
                        stickerId = "52002752";
                        break;
                    case 8:
                        packageId = "11538";
                        stickerId = "51626498";
                        break;
                    case 9:
                        packageId = "11538";
                        stickerId = "51626501";
                        break;
                    case 10:
                        packageId = "11539";
                        stickerId = "52114117";
                        break;
                    default:
                        packageId = "446";
                        stickerId = "2000";
                        break;
                }
                log.info("sport : packageId {}: stickerId:{}", packageId, stickerId);
                reply(replyToken, new StickerMessage(packageId, stickerId));
                break;
            }
            case 999: {
                Source source = event.getSource();
                if (source instanceof GroupSource) {
                    this.replyText(replyToken, "Leaving group");
                    lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
                } else if (source instanceof RoomSource) {
                    this.replyText(replyToken, "Leaving room");
                    lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
                } else {
                    this.replyText(replyToken, "Bot can't leave from 1:1 chat");
                }
                break;
            }
            case 2 :
                log.info("Returns echo message {}: {}", replyToken, text);
                this.replyText(replyToken, text);
                break;
            case 3 : {
                final String userId = event.getSource().getUserId();
                if (userId != null) {
                    if (event.getSource() instanceof GroupSource) {
                        lineMessagingClient
                                .getGroupMemberProfile(((GroupSource) event.getSource()).getGroupId(), userId)
                                .whenComplete((profile, throwable) -> {
                                    if (throwable != null) {
                                        this.replyText(replyToken, throwable.getMessage());
                                        return;
                                    }

                                    this.reply(replyToken,
                                            TextMessage.builder()
                                                    .text(this.getNickname(profile.getDisplayName()) + getBeginning())
                                                    .build());
                                });
                    }
                }
                break;
            }
            default:
                log.info("Unknown message {}", text);
                break;
        }
    }

    private static URI createUri(String path) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                                          .scheme("https")
                                          .path(path).build()
                                          .toUri();
    }

    private void system(String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            log.info("result: {} =>  {}", Arrays.toString(args), i);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            log.info("Interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
        log.info("Got content-type: {}", responseBody);

        DownloadedContent tempFile = createTempFile(ext);
        try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
            ByteStreams.copy(responseBody.getStream(), outputStream);
            log.info("Saved {}: {}", ext, tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DownloadedContent createTempFile(String ext) {
        String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID() + '.' + ext;
        Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
        tempFile.toFile().deleteOnExit();
        return new DownloadedContent(
                tempFile,
                createUri("/downloaded/" + tempFile.getFileName()));
    }

    @Value
    private static class DownloadedContent {
        Path path;
        URI uri;
    }

    private String getBeginning() {
        Random rand = new Random();
        int upperbound = sentence.size();
        int random = rand.nextInt(upperbound);

        return sentence.get(random);
    }

    private String getNickname(final String displayName) {
        return nickname.getOrDefault(displayName, displayName);
    }
}
