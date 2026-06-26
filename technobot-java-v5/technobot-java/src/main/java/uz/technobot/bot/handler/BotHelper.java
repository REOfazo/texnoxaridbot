package uz.technobot.bot.handler;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public abstract class BotHelper {

    private static final int MAX_CAPTION = 1024;
    private static final int MAX_TEXT    = 4096;

    // ── Xabar yuborish ────────────────────────────────────────

    protected void send(AbsSender bot, Long chatId, String text) {
        send(bot, chatId, text, null);
    }

    protected void send(AbsSender bot, Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(truncate(text, MAX_TEXT));
        msg.setParseMode("Markdown");
        msg.setDisableWebPagePreview(true);
        if (keyboard != null) msg.setReplyMarkup(keyboard);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            // Markdown parse xatosi bo'lsa plain text bilan qayta urinish
            if (e.getMessage().contains("parse")) {
                try {
                    msg.setParseMode(null);
                    bot.execute(msg);
                } catch (TelegramApiException ex) {
                    log.error("send xatosi: {}", ex.getMessage());
                }
            } else {
                log.error("send xatosi: {}", e.getMessage());
            }
        }
    }

    // ── Rasm yuborish ─────────────────────────────────────────

    protected void sendPhoto(AbsSender bot, Long chatId, String fileId,
                             String caption, InlineKeyboardMarkup keyboard) {
        SendPhoto sp = new SendPhoto();
        sp.setChatId(chatId.toString());
        sp.setPhoto(new InputFile(fileId));
        sp.setCaption(truncate(caption, MAX_CAPTION));
        sp.setParseMode("Markdown");
        if (keyboard != null) sp.setReplyMarkup(keyboard);
        try {
            bot.execute(sp);
        } catch (TelegramApiException e) {
            log.error("sendPhoto xatosi: {}", e.getMessage());
            // Rasm o'rniga matn yuboramiz
            send(bot, chatId, caption, keyboard);
        }
    }

    // ── Xabarni tahrirlash ────────────────────────────────────

    protected void editText(AbsSender bot, Long chatId, Integer messageId,
                            String text, InlineKeyboardMarkup keyboard) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setText(truncate(text, MAX_TEXT));
        edit.setParseMode("Markdown");
        edit.setDisableWebPagePreview(true);
        if (keyboard != null) edit.setReplyMarkup(keyboard);
        try {
            bot.execute(edit);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message is not modified")) {
                log.warn("editText xatosi: {}", e.getMessage());
            }
        }
    }

    protected void editCaption(AbsSender bot, Long chatId, Integer messageId,
                               String caption, InlineKeyboardMarkup keyboard) {
        EditMessageCaption edit = new EditMessageCaption();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setCaption(truncate(caption, MAX_CAPTION));
        edit.setParseMode("Markdown");
        if (keyboard != null) edit.setReplyMarkup(keyboard);
        try {
            bot.execute(edit);
        } catch (TelegramApiException e) {
            if (!e.getMessage().contains("message is not modified")) {
                log.warn("editCaption xatosi: {}", e.getMessage());
            }
        }
    }

    protected void deleteMsg(AbsSender bot, Long chatId, Integer messageId) {
        DeleteMessage del = new DeleteMessage();
        del.setChatId(chatId.toString());
        del.setMessageId(messageId);
        try { bot.execute(del); } catch (TelegramApiException ignore) {}
    }

    // ── Callback javob ────────────────────────────────────────

    protected void answerCallback(AbsSender bot, String callbackQueryId, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQueryId);
        if (text != null && !text.isBlank()) answer.setText(truncate(text, 200));
        try { bot.execute(answer); } catch (TelegramApiException ignore) {}
    }

    // ── Yordamchi ─────────────────────────────────────────────

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() > max ? text.substring(0, max - 3) + "..." : text;
    }
}
