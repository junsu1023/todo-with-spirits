using System;
using System.IO;
using NUnit.Framework;
using TodoSpirits.Core;
using UnityEngine;

namespace TodoSpirits.Runtime.Tests
{
    public sealed class JsonSpiritSaveRepositoryTests
    {
        private string _directory;
        private string _path;
        [SetUp]
        public void Setup()
        {
            _directory = Path.GetFullPath(Path.Combine(Application.dataPath, "../Temp/CompanionSaveTests-" + Guid.NewGuid().ToString("N")));
            Directory.CreateDirectory(_directory);
            _path = Path.Combine(_directory, "save.json");
        }
        [TearDown]
        public void Cleanup()
        {
            foreach (string suffix in new[] { "", ".bak", ".tmp" })
                if (File.Exists(_path + suffix)) File.Delete(_path + suffix);
            if (Directory.Exists(_directory)) Directory.Delete(_directory);
        }
        private static PrototypeSaveData Data(int balance) => new PrototypeSaveData { EssenceWallet = new EssenceWallet { Balance = balance } };
        [Test]
        public void Save_ReplacesWholeFileAndRetainsPreviousVersion()
        {
            var repository = new JsonSpiritSaveRepository(_path);
            repository.Save(Data(10)); repository.Save(Data(25));
            Assert.That(repository.Load().EssenceWallet.Balance, Is.EqualTo(25));
            Assert.That(new JsonSpiritSaveRepository(_path + ".bak").Load().EssenceWallet.Balance, Is.EqualTo(10));
            Assert.That(File.Exists(_path + ".tmp"), Is.False);
        }
        [Test]
        public void CorruptPrimary_RecoversBackupAndDoesNotOverwriteItWithCorruption()
        {
            var repository = new JsonSpiritSaveRepository(_path);
            repository.Save(Data(10)); repository.Save(Data(25));
            File.WriteAllText(_path, "{broken");
            var restored = repository.Load();
            Assert.That(restored.EssenceWallet.Balance, Is.EqualTo(10));
            repository.Save(restored);
            Assert.That(repository.Load().EssenceWallet.Balance, Is.EqualTo(10));
            Assert.That(new JsonSpiritSaveRepository(_path + ".bak").Load().EssenceWallet.Balance, Is.EqualTo(10));
        }
        [Test]
        public void MissingPrimary_RecoversBackup_WhileUnrecoverableDataIsPreserved()
        {
            var repository = new JsonSpiritSaveRepository(_path);
            repository.Save(Data(10)); repository.Save(Data(25));
            File.Delete(_path);
            Assert.That(repository.Load().EssenceWallet.Balance, Is.EqualTo(10));
            File.WriteAllText(_path + ".bak", "{}");
            Assert.Throws<IOException>(() => repository.Load());
            Assert.That(File.ReadAllText(_path + ".bak"), Is.EqualTo("{}"));
        }
    }
}
