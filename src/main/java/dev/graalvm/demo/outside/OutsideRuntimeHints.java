package dev.graalvm.demo.outside;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.NioEndpoint;
import org.springaicommunity.typesafe.JsonContent;
import org.springaicommunity.typesafe.exception.TypeSafeErrorDetail;
import org.springaicommunity.typesafe.question.Choice;
import org.springaicommunity.typesafe.question.Noul;
import org.springaicommunity.typesafe.question.NoulCriteria;
import org.springaicommunity.typesafe.question.Question;
import org.springaicommunity.typesafe.question.QuestionType;
import org.springaicommunity.typesafe.question.Score;
import org.springaicommunity.typesafe.question.SystemOneRequest;
import org.springaicommunity.typesafe.response.Answer;
import org.springaicommunity.typesafe.response.AnswerType;
import org.springaicommunity.typesafe.response.ChoiceAnswer;
import org.springaicommunity.typesafe.response.ListModelsResponse;
import org.springaicommunity.typesafe.response.ModelMetadata;
import org.springaicommunity.typesafe.response.NoulAnswer;
import org.springaicommunity.typesafe.response.ScoreAnswer;
import org.springaicommunity.typesafe.response.SystemOneResponse;
import org.springaicommunity.typesafe.response.UnknownAnswer;
import org.springaicommunity.typesafe.response.Usage;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

public final class OutsideRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        Class<?>[] tomcatProtocolTypes = {
                Connector.class,
                AbstractProtocol.class,
                AbstractHttp11Protocol.class,
                Http11NioProtocol.class,
                AbstractEndpoint.class,
                NioEndpoint.class
        };
        for (Class<?> type : tomcatProtocolTypes) {
            hints.reflection().registerType(type,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);
        }

        Class<?>[] jsonTypes = {
                JsonContent.class,
                Question.class, QuestionType.class,
                SystemOneRequest.class, Choice.class, Noul.class, NoulCriteria.class, Score.class,
                Answer.class, AnswerType.class,
                SystemOneResponse.class, ChoiceAnswer.class, NoulAnswer.class, ScoreAnswer.class,
                UnknownAnswer.class, Usage.class, ListModelsResponse.class, ModelMetadata.class,
                TypeSafeErrorDetail.class, TypeSafeErrorDetail.ValidationError.class
        };
        for (Class<?> type : jsonTypes) {
            hints.reflection().registerType(type,
                    MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_DECLARED_METHODS,
                    MemberCategory.ACCESS_PUBLIC_FIELDS,
                    MemberCategory.ACCESS_DECLARED_FIELDS);
        }

        // Package-private SDK type instantiated reflectively by Jackson.
        hints.reflection().registerType(
                TypeReference.of("org.springaicommunity.typesafe.response.AnswerDeserializer"),
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                MemberCategory.INVOKE_DECLARED_METHODS);
    }
}
