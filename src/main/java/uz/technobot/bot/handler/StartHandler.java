package uz.technobot.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;
import uz.technobot.entity.BotUser;
import uz.technobot.enums.UserState;
import uz.technobot.service.SessionService;
import uz.technobot.service.UserService;
import uz.technobot.util.Keyboards;
import uz.technobot.util.Msg;

@Component
@RequiredArgsConstructor
public class StartHandler extends BotHelper {

    private final UserService userService;
    private final SessionService session;

    public void handle(Update update, AbsSender bot) {
        Message msg = update.getMessage();
        Long chatId = msg.getChatId();
        var from = msg.getFrom();

        BotUser user = userService.getOrCreate(
                from.getId(),
                from.getUserName(),
                (from.getFirstName() + " " + (from.getLastName() != null ? from.getLastName() : "")).trim()
        );

        // Saqlangan tilni yuklaymiz
        String lang = user.getLanguage().code();
        session.setLang(from.getId(), lang);
        session.setState(from.getId(), UserState.IDLE);

        send(bot, chatId, Msg.get("choose_lang", lang), Keyboards.language());
    }

    public void handleLanguageSelect(Update update, AbsSender bot) {
        var query = update.getCallbackQuery();
        Long userId = query.getFrom().getId();
        Long chatId = query.getMessage().getChatId();
        String lang = query.getData().split(":")[1]; // "lang:uz" → "uz"

        answerCallback(bot, query.getId(), null);

        userService.updateLanguage(userId, lang);
        session.setLang(userId, lang);
        session.setState(userId, UserState.IDLE);

        String name = query.getFrom().getFirstName();
        send(bot, chatId, Msg.get("welcome", lang, "name", name), Keyboards.mainMenu(lang));
    }

    public void showMainMenu(Update update, AbsSender bot) {
        Long userId, chatId;
        if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            chatId = update.getCallbackQuery().getMessage().getChatId();
            answerCallback(bot, update.getCallbackQuery().getId(), null);
        } else {
            userId = update.getMessage().getFrom().getId();
            chatId = update.getMessage().getChatId();
        }
        String lang = session.getLang(userId);
        session.setState(userId, UserState.IDLE);
        send(bot, chatId, Msg.get("main_menu", lang), Keyboards.mainMenu(lang));
    }
}
