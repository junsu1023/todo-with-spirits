#if UNITY_EDITOR
using System;
using System.Linq;
using TodoSpirits.Presentation.Forest;
using TodoSpirits.Presentation.Main;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Editor
{
    public static class ForestSceneInstaller
    {
        private const string Root = "Assets/TodoSpirits/";
        [MenuItem("TodoSpirits/Install Forest UI")]
        public static void Install()
        {
            if (EditorApplication.isPlaying) throw new InvalidOperationException("Play 모드를 먼저 종료해 주세요.");
            var scene = UnityEngine.SceneManagement.SceneManager.GetSceneByPath(Root + "Scenes/01_Main.unity");
            if (scene.IsValid() && scene.isDirty) throw new InvalidOperationException("01_Main의 기존 편집 내용을 먼저 저장해 주세요.");
            if (!scene.IsValid() || !scene.isLoaded) scene = EditorSceneManager.OpenScene(Root+"Scenes/01_Main.unity",OpenSceneMode.Additive);
            var background = ImportBackground(); var poses = ImportPoses(); var props = ImportProps(); var journey = ImportJourneyProps();
            var gifts = ImportGiftProps(); var lunch = ImportLunch();
            var temp = new GameObject("PF_UI_ForestApp",typeof(RectTransform),typeof(ForestAppView));
            var rect = (RectTransform)temp.transform; rect.anchorMin=Vector2.zero;rect.anchorMax=Vector2.one;rect.offsetMin=rect.offsetMax=Vector2.zero;
            var serialized = new SerializedObject(temp.GetComponent<ForestAppView>());
            serialized.FindProperty("regularFont").objectReferenceValue=AssetDatabase.LoadAssetAtPath<Font>("Assets/Fonts/NotoSansKR-Regular.ttf");
            serialized.FindProperty("boldFont").objectReferenceValue=AssetDatabase.LoadAssetAtPath<Font>("Assets/Fonts/NotoSansKR-Bold.ttf");
            serialized.FindProperty("forestBackground").objectReferenceValue=background;
            var array=serialized.FindProperty("spiritPoses");array.arraySize=4;
            for(int i=0;i<4;i++)array.GetArrayElementAtIndex(i).objectReferenceValue=poses[i];
            var propArray=serialized.FindProperty("forestProps");propArray.arraySize=props.Length;
            for(int i=0;i<props.Length;i++)propArray.GetArrayElementAtIndex(i).objectReferenceValue=props[i];
            var journeyArray=serialized.FindProperty("journeyProps");journeyArray.arraySize=journey.Length;
            for(int i=0;i<journey.Length;i++)journeyArray.GetArrayElementAtIndex(i).objectReferenceValue=journey[i];
            var giftArray=serialized.FindProperty("giftProps");giftArray.arraySize=gifts.Length;
            for(int i=0;i<gifts.Length;i++)giftArray.GetArrayElementAtIndex(i).objectReferenceValue=gifts[i];
            serialized.FindProperty("tripLunchSprite").objectReferenceValue=lunch;
            serialized.ApplyModifiedPropertiesWithoutUndo();
            string prefabPath=Root+"Prefabs/UI/PF_UI_ForestApp.prefab";
            var prefab=PrefabUtility.SaveAsPrefabAsset(temp,prefabPath);UnityEngine.Object.DestroyImmediate(temp);
            var canvas=scene.GetRootGameObjects().SelectMany(r=>r.GetComponentsInChildren<Canvas>(true)).Single();
            // Keep the old authored controller and its references as an inactive rollback point.
            foreach(var old in scene.GetRootGameObjects().SelectMany(r=>r.GetComponentsInChildren<MainSceneController>(true)))old.gameObject.SetActive(false);
            foreach(var old in canvas.GetComponentsInChildren<ForestAppView>(true))UnityEngine.Object.DestroyImmediate(old.gameObject);
            PrefabUtility.InstantiatePrefab(prefab,canvas.transform);
            canvas.renderMode=RenderMode.ScreenSpaceOverlay;
            var scaler=canvas.GetComponent<CanvasScaler>();scaler.uiScaleMode=CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution=new Vector2(480,900);scaler.screenMatchMode=CanvasScaler.ScreenMatchMode.MatchWidthOrHeight;scaler.matchWidthOrHeight=0;
            EditorSceneManager.MarkSceneDirty(scene);EditorSceneManager.SaveScene(scene);AssetDatabase.SaveAssets();
            var boot = UnityEngine.SceneManagement.SceneManager.GetSceneByPath(Root+"Scenes/00_Boot.unity");
            if (boot.IsValid() && boot.isDirty) throw new InvalidOperationException("00_Boot의 기존 편집 내용을 먼저 저장해 주세요.");
            if (!boot.IsValid() || !boot.isLoaded) boot=EditorSceneManager.OpenScene(Root+"Scenes/00_Boot.unity",OpenSceneMode.Additive);
            var bootstrap=boot.GetRootGameObjects().SelectMany(r=>r.GetComponentsInChildren<TodoSpirits.Runtime.PrototypeBootstrap>(true)).Single();
            if(bootstrap.GetComponent<TodoSpirits.Runtime.ForestTodoBridge>()==null)bootstrap.gameObject.AddComponent<TodoSpirits.Runtime.ForestTodoBridge>();
            EditorSceneManager.MarkSceneDirty(boot);EditorSceneManager.SaveScene(boot);
        }
        private static Sprite ImportBackground()
        {
            string path=Root+"Art/Forest/forest-background-storybook.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Single;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=2048;importer.textureCompression=TextureImporterCompression.Compressed;importer.SaveAndReimport();
            return AssetDatabase.LoadAssetAtPath<Sprite>(path);
        }
        private static Sprite[] ImportProps()
        {
            string path=Root+"Art/Forest/forest-props-release.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);
            importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Multiple;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=2048;
            importer.textureCompression=TextureImporterCompression.Compressed;
            string[] names={"forest_books","forest_tools","forest_tea","forest_plant","forest_cushion","forest_letter"};
#pragma warning disable CS0618
            importer.spritesheet=names.Select((name,i)=>new SpriteMetaData {name=name,rect=new Rect(i%3*512,(1-i/3)*512,512,512),pivot=Vector2.one*.5f,alignment=0}).ToArray();
#pragma warning restore CS0618
            importer.SaveAndReimport();
            var sprites=AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToArray();
            return names.Select(name=>sprites.Single(s=>s.name==name)).ToArray();
        }
        private static Sprite ImportLunch()
        {
            string path=Root+"Art/Forest/forest-lunch.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);
            importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Single;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=1024;
            importer.textureCompression=TextureImporterCompression.Compressed;importer.SaveAndReimport();
            return AssetDatabase.LoadAssetAtPath<Sprite>(path);
        }
        private static Sprite[] ImportGiftProps()
        {
            string path=Root+"Art/Forest/forest-gifts.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);
            importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Multiple;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=2048;
            importer.textureCompression=TextureImporterCompression.Compressed;
            string[] names={"gift_book","gift_blanket","gift_paints","gift_plant","gift_tea","gift_stone"};
#pragma warning disable CS0618
            importer.spritesheet=names.Select((name,i)=>new SpriteMetaData {name=name,rect=new Rect(i%3*512,(1-i/3)*512,512,512),pivot=Vector2.one*.5f,alignment=0}).ToArray();
#pragma warning restore CS0618
            importer.SaveAndReimport();
            var sprites=AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToArray();
            return names.Select(name=>sprites.Single(s=>s.name==name)).ToArray();
        }
        private static Sprite[] ImportJourneyProps()
        {
            string path=Root+"Art/Forest/forest-journey-props.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);
            importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Multiple;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=2048;
            importer.textureCompression=TextureImporterCompression.Compressed;
            string[] names={"journey_notebook","journey_backpack","journey_pinwheel","gift_bookmark","gift_map","gift_chime","egg_sunshine","egg_dew","egg_wind"};
            // Generated atlas is 1254 px square: equal 418 px cells, in reading order.
#pragma warning disable CS0618
            importer.spritesheet=names.Select((name,i)=>new SpriteMetaData {name=name,rect=new Rect(i%3*418,(2-i/3)*418,418,418),pivot=Vector2.one*.5f,alignment=0}).ToArray();
#pragma warning restore CS0618
            importer.SaveAndReimport();
            var sprites=AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToArray();
            return names.Select(name=>sprites.Single(s=>s.name==name)).ToArray();
        }
        private static Sprite[] ImportPoses()
        {
            string path=Root+"Art/Forest/wind-spirit-expressions.png";
            var importer=(TextureImporter)AssetImporter.GetAtPath(path);importer.textureType=TextureImporterType.Sprite;importer.spriteImportMode=SpriteImportMode.Multiple;
            importer.alphaIsTransparency=true;importer.mipmapEnabled=false;importer.maxTextureSize=2048;importer.textureCompression=TextureImporterCompression.Uncompressed;
            var names=new[]{"wind_idle","wind_blink","wind_happy","wind_curious"};
            // Identical canvas extents preserve registration when swapping facial poses.
            var rects=new[]{new Rect(90,679,490,495),new Rect(653,679,490,495),new Rect(90,115,490,495),new Rect(653,115,490,495)};
#pragma warning disable CS0618
            importer.spritesheet=names.Select((name,i)=>new SpriteMetaData{name=name,rect=rects[i],pivot=Vector2.one*.5f,alignment=0}).ToArray();
#pragma warning restore CS0618
            importer.SaveAndReimport();
            var sprites=AssetDatabase.LoadAllAssetsAtPath(path).OfType<Sprite>().ToArray();
            return names.Select(name=>sprites.Single(s=>s.name==name)).ToArray();
        }
    }
}
#endif
