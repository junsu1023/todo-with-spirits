#if UNITY_EDITOR
using System;
using System.Reflection;
using UnityEditor;
using UnityEngine;

namespace TodoSpirits.Editor
{
    public static class ForestEditorPreview
    {
        public static void SetSize(int width, int height)
        {
            const BindingFlags flags = BindingFlags.Public | BindingFlags.NonPublic | BindingFlags.Instance;
            var assembly=typeof(UnityEditor.Editor).Assembly;
            var sizesType=assembly.GetType("UnityEditor.GameViewSizes");
            var singleton=typeof(ScriptableSingleton<>).MakeGenericType(sizesType);
            var sizes=singleton.GetProperty("instance",BindingFlags.Public|BindingFlags.Static).GetValue(null);
            var group=sizesType.GetProperty("currentGroup",flags).GetValue(sizes);
            var sizeType=assembly.GetType("UnityEditor.GameViewSize");
            var kind=assembly.GetType("UnityEditor.GameViewSizeType");
            string title="Forest QA "+width+"x"+height;
            string[] labels=(string[])group.GetType().GetMethod("GetDisplayTexts").Invoke(group,null);
            int index=Array.FindIndex(labels,s=>s.Contains(title));
            if(index<0){var size=Activator.CreateInstance(sizeType,flags,null,new object[]{Enum.Parse(kind,"FixedResolution"),width,height,title},null);group.GetType().GetMethod("AddCustomSize").Invoke(group,new[]{size});index=(int)group.GetType().GetMethod("GetTotalCount").Invoke(group,null)-1;}
            var view=EditorWindow.GetWindow(assembly.GetType("UnityEditor.GameView"));
            view.GetType().GetProperty("selectedSizeIndex",flags).SetValue(view,index);view.Focus();view.Repaint();
        }
    }
}
#endif
