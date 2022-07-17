package tech.bananaz.bot.models;

import javax.persistence.*;
import lombok.Data;
import lombok.ToString;

@ToString(includeFieldNames=true)
@Data
@Entity
@Table(name="listing")
public class ListingConfig {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long    id;
	@Column(nullable = false)
	private String  contractAddress;
	@Column(nullable = false, name = "`interval`", columnDefinition = "INT UNSIGNED")
	private Integer interval;
	@Column(columnDefinition = "INT UNSIGNED")
	private Integer discordMessageColor;
	private String  discordServerId;
	private String  discordChannelId;
	private String  discordToken;
	private String  twitterApiKey;
	private String  twitterApiKeySecret;
	private String  twitterAccessToken;
	private String  twitterAccessTokenSecret;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 1")
	private Boolean showBundles;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean autoRarity;
	private String  raritySlugOverwrite;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean excludeOpensea;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean excludeLooksrare;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean excludeDiscord;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean excludeTwitter;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean isSlug;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 0")
	private Boolean solanaOnOpensea;
	@Column(columnDefinition="BIGINT UNSIGNED DEFAULT 0")
	private Long    lastLooksId;
	@Column(columnDefinition="BIGINT UNSIGNED DEFAULT 0")
	private Long    lastOpenseaId;
	@Column(nullable = false, columnDefinition="TINYINT(1) UNSIGNED DEFAULT 1")
	private Boolean active;

}
