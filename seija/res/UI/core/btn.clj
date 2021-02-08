(button  :OnTouchEnd #(emit-this :btn-touch-end)
         :OnTouchStart #(emit-this :btn-touch)
         :OnMouseEnter #(emit-this :btn-enter)
         :OnMouseLeave #(emit-this :btn-exit)
  :children [
      (template
          :children [
                (sprite :type (sliced 8 8 8 8) :sheet (env res.sheet) :spriteName "button"
                  :color (attr-fbind :buttonState 
                            #(match %
                                "Normal" (hex-color "#111111")
                                "Hover"  (hex-color "#222222")
                                "Press"  (hex-color "#000000")
                             )
                         )
                )
                (label :color "#ffffff" :font (env res.font) :text "新建文件夹")
          ]
      )
  ]
)