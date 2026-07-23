using Newtonsoft.Json;

namespace TodoSpirits.Unity.Runtime.Spirit
{
    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SpiritAttributes
    {
        [JsonProperty("focus", Required = Required.Always)] public int Focus { get; set; }
        [JsonProperty("vitality", Required = Required.Always)] public int Vitality { get; set; }
        [JsonProperty("consistency", Required = Required.Always)] public int Consistency { get; set; }
        [JsonProperty("creativity", Required = Required.Always)] public int Creativity { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SpiritStateSnapshot
    {
        [JsonProperty("id", Required = Required.Always)] public string Id { get; set; } = string.Empty;
        [JsonProperty("name", Required = Required.Always)] public string Name { get; set; } = string.Empty;
        [JsonProperty("stage", Required = Required.Always)] public int Stage { get; set; }
        [JsonProperty("exp", Required = Required.Always)] public int Exp { get; set; }
        [JsonProperty("maxExp", Required = Required.Always)] public int MaxExp { get; set; }
        [JsonProperty("attributes", Required = Required.Always)] public SpiritAttributes Attributes { get; set; } = new SpiritAttributes();
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SpiritAppearanceSnapshot
    {
        [JsonProperty("modelId", Required = Required.Always)] public string ModelId { get; set; } = string.Empty;
        [JsonProperty("skinId", Required = Required.Always)] public string SkinId { get; set; } = string.Empty;
        [JsonProperty("growthStageId", Required = Required.Always)] public string GrowthStageId { get; set; } = string.Empty;
        [JsonProperty("environmentId", Required = Required.Always)] public string EnvironmentId { get; set; } = string.Empty;
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SpiritAudioSnapshot
    {
        [JsonProperty("masterVolume", Required = Required.Always)] public double MasterVolume { get; set; }
        [JsonProperty("muted", Required = Required.Always)] public bool Muted { get; set; }
    }

    [JsonObject(MemberSerialization.OptIn)]
    public sealed class SpiritSession
    {
        [JsonProperty("schemaVersion", Required = Required.Always)] public int SchemaVersion { get; set; }
        [JsonProperty("locale", Required = Required.Always)] public string Locale { get; set; } = string.Empty;
        [JsonProperty("theme", Required = Required.Always)] public string Theme { get; set; } = string.Empty;
        [JsonProperty("spirit", Required = Required.Always)] public SpiritStateSnapshot Spirit { get; set; } = new SpiritStateSnapshot();
        [JsonProperty("appearance", Required = Required.Always)] public SpiritAppearanceSnapshot Appearance { get; set; } = new SpiritAppearanceSnapshot();
        [JsonProperty("audio", Required = Required.Always)] public SpiritAudioSnapshot Audio { get; set; } = new SpiritAudioSnapshot();
    }
}

